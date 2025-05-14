package org.example.orderservice.services;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.orderservice.configs.kafka.KafkaPublisher;
import org.example.orderservice.dtos.*;
import org.example.orderservice.models.*;
import org.example.orderservice.repositories.CartRepository;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
import org.example.orderservice.repositories.OrderAuditLogRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final KafkaPublisher kafkaPublisher;
    private final OrderAuditLogRepository auditLogRepository;
    private final MeterRegistry meterRegistry;
    private final EmailService emailService;
    private final InventoryClient inventoryClient;
    private final InventoryRollbackTaskRepository rollbackTaskRepository;
    private final UserProfileClient userProfileClient;




    public OrderService(CartRepository cartRepository,
                        OrderRepository orderRepository,
                        ProductClient productClient,
                        KafkaPublisher kafkaPublisher,
                        OrderAuditLogRepository auditLogRepository,
                        MeterRegistry meterRegistry,
                        EmailService emailService,
                        InventoryClient inventoryClient,
                        InventoryRollbackTaskRepository rollbackTaskRepository,
                        UserProfileClient userProfileClient) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.kafkaPublisher = kafkaPublisher;
        this.auditLogRepository = auditLogRepository;
        this.meterRegistry = meterRegistry;
        this.emailService = emailService;
        this.inventoryClient = inventoryClient;
        this.rollbackTaskRepository = rollbackTaskRepository;
        this.userProfileClient = userProfileClient;
    }


    @Transactional
    public Cart addToCart(String userId, CartItemRequestDTO cartItemRequest) {
        var product = productClient.getProductDetails(cartItemRequest.getProductId());

        if ("UNAVAILABLE".equalsIgnoreCase(product.getName())) {
            logger.warn("🚫 Product {} could not be fetched. Skipping cart add for user {}", cartItemRequest.getProductId(), userId);
            throw new RuntimeException("Product is temporarily unavailable. Please try again later.");
        }

        Cart cart = cartRepository.findByUserId(userId).orElse(new Cart(userId));

        CartItem cartItem = new CartItem(cartItemRequest.getProductId(), cartItemRequest.getQuantity());
        cartItem.setProductName(product.getName());
        cartItem.setUnitPrice(product.getPrice());
        cartItem.setCurrency(product.getCurrency());

        cart.addItem(cartItem);
        logger.info("Added item to cart for user {}: {}", userId, cartItem.getProductName());
        return cartRepository.save(cart);
    }

    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId).orElse(new Cart(userId));
    }

    @Transactional
    public Cart removeItemFromCart(String userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        logger.info("Removed item {} from cart for user {}", itemId, userId);
        return cartRepository.save(cart);
    }

    @Transactional
    public Order placeOrder(String userId, OrderRequestDTO orderRequest, String bearerToken) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        UserProfileDTO profile = userProfileClient.getUserProfile(bearerToken);
        UserProfileDTO.AddressDTO addr = profile.getAddress();

        ShippingAddress address = new ShippingAddress(
                addr.getStreet(),
                addr.getCity(),
                addr.getState(),
                addr.getZipCode()
        );

        Order order = new Order(userId, address);

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(cartItem.getProductId(), cartItem.getQuantity());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setCurrency(cartItem.getCurrency());
            order.addOrderItem(orderItem);
        }

        Order saved = orderRepository.save(order);
        cartRepository.delete(cart);
        logger.info("Placed order {} for user {}", saved.getId(), userId);
        kafkaPublisher.publishOrderPlaced(saved);
        auditLogRepository.save(new OrderAuditLog(saved.getId(), userId, "PLACED"));
        meterRegistry.counter("orders.placed.total").increment();

        emailService.sendOrderConfirmationEmail(
                profile.getEmail(),
                "Your Order #" + saved.getId() + " has been placed!",
                "Thank you for your order. We'll ship it soon."
        );

        return saved;
    }


    public List<Order> getOrdersForUser(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order getOrderByIdAndUserId(Long orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to access this order");
        }
        return order;
    }

    @Transactional
    public Order cancelOrder(Long orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new RuntimeException("Only placed orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        kafkaPublisher.publishOrderCancelled(order);
        auditLogRepository.save(new OrderAuditLog(orderId, userId, "CANCELLED"));
        meterRegistry.counter("orders.cancelled.total").increment();

        // ✅ Inventory rollback logic
        List<Long> productIds = order.getOrderItems().stream()
                .map(OrderItem::getProductId)
                .toList();

        RollbackRequestDTO rollbackRequest = new RollbackRequestDTO(productIds);
        inventoryClient.rollbackStock(rollbackRequest);
        InventoryRollbackTask task = new InventoryRollbackTask();
        task.setOrderId(order.getId());
        task.setProductIds(productIds);
        rollbackTaskRepository.save(task);


        return orderRepository.save(order);
    }

    public Page<Order> getOrdersForUser(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus, String adminUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot update a cancelled order");
        }

        if (order.getStatus() == newStatus) {
            return order; // No change
        }

        if (newStatus == OrderStatus.SHIPPED) {
            throw new RuntimeException("SHIPPED status is set automatically via payment event");
        }

        order.setStatus(newStatus);
        logger.info("✅ Order {} status manually updated to {}", orderId, newStatus);

        auditLogRepository.save(new OrderAuditLog(orderId, adminUsername, newStatus.name()));
        meterRegistry.counter("orders.status.updated", "status", newStatus.name()).increment();


        return orderRepository.save(order);
    }

    public List<OrderAuditLogDTO> getAuditLogsForOrder(Long orderId) {
        List<OrderAuditLog> logs = auditLogRepository.findByOrderIdOrderByCreatedAtDesc(orderId);

        return logs.stream().map(OrderAuditLogDTO::fromEntity).toList();
    }



}
