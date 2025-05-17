package org.example.orderservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.example.orderservice.clients.InventoryClient;
import org.example.orderservice.clients.ProductClient;
import org.example.orderservice.clients.UserProfileClient;
import org.example.orderservice.kafka.KafkaPublisher;
import org.example.orderservice.dtos.*;
import org.example.orderservice.models.*;
import org.example.orderservice.repositories.CartRepository;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
import org.example.orderservice.repositories.OrderAuditLogRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
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
    private final SendGridEmailService emailService;
    private final InventoryClient inventoryClient;
    private final InventoryRollbackTaskRepository rollbackTaskRepository;
    private final UserProfileClient userProfileClient;





    public OrderService(CartRepository cartRepository,
                        OrderRepository orderRepository,
                        ProductClient productClient,
                        KafkaPublisher kafkaPublisher,
                        OrderAuditLogRepository auditLogRepository,
                        MeterRegistry meterRegistry,
                        SendGridEmailService emailService,
                        InventoryClient inventoryClient,
                        InventoryRollbackTaskRepository rollbackTaskRepository,
                        UserProfileClient userProfileClient
                        ) {
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

    @CacheEvict(value = "userCart", key = "#userId")
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

    @Cacheable(value = "userCart", key = "#userId")
    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId).orElse(new Cart(userId));
    }

    @CacheEvict(value = "userCart", key = "#userId")
    @Transactional
    public Cart removeItemFromCart(String userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        logger.info("Removed item {} from cart for user {}", itemId, userId);
        return cartRepository.save(cart);
    }

    @CacheEvict(value = "userCart", key = "#userId")
    @Transactional
    public Order placeOrder(String userId, OrderRequestDTO orderRequest, String bearerToken) throws IOException {
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

        try {
            emailService.sendEmail(
                    profile.getEmail(), // Already retrieved from userProfileClient
                    "🎉 Your Order #" + saved.getId() + " is Confirmed!",
                    """
                    Thank you for shopping with us!
            
                    📦 Order ID: %s
                    👤 Name: %s
                    🏠 Shipping To: %s, %s, %s, %s
                    💰 Total Items: %d
            
                    We'll notify you once your order is shipped. Visit your dashboard to track the order.
            
                    -- YourShop Team
                    """.formatted(
                            saved.getId(),
                            profile.getName(),
                            addr.getStreet(),
                            addr.getCity(),
                            addr.getState(),
                            addr.getZipCode(),
                            saved.getOrderItems().size()
                    )
            );
        } catch (IOException e) {
            logger.warn("❌ Failed to send order confirmation email for order {}: {}", saved.getId(), e.getMessage());
        }


        return saved;
    }


    public List<Order> getOrdersForUser(String userId) {
        return orderRepository.findByUserId(userId);
    }

    @Cacheable(value = "orderDetails", key = "#orderId + '-' + #userId")
    public Order getOrderByIdAndUserId(Long orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to access this order");
        }
        return order;
    }

    @CacheEvict(value = { "orders", "orderDetails", "orderAuditLogs" }, allEntries = true)
    @Transactional
    public Order cancelOrder(Long orderId, String userId) throws JsonProcessingException {
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
        try {
            UserProfileDTO profile = userProfileClient.getUserProfile("Bearer " + userId);
            emailService.sendEmail(
                    profile.getEmail(),
                    "❌ Order #" + orderId + " Cancelled",
                    "Your order has been cancelled successfully. If this wasn't you, please contact support immediately."
            );

        } catch (IOException e) {
            logger.warn("❌ Failed to send order cancellation email for order {}: {}", orderId, e.getMessage());
        }


        // ✅ Inventory rollback logic
        List<RollbackStockRequestDto.ProductRollbackEntry> entries = order.getOrderItems().stream()
                .map(item -> new RollbackStockRequestDto.ProductRollbackEntry(item.getProductId(), item.getQuantity()))
                .toList();

        RollbackStockRequestDto rollbackRequest = new RollbackStockRequestDto(entries, "Cancelled by user");

        try {
            inventoryClient.rollbackStock(rollbackRequest);
            logger.info("🔁 Rollback API called after user cancellation.");
        } catch (Exception ex) {
            logger.warn("❗ Failed to rollback stock. Queuing for retry: {}", ex.getMessage());

            InventoryRollbackTask task = new InventoryRollbackTask();
            task.setOrderId(order.getId());
            task.setPayload(new ObjectMapper().writeValueAsString(rollbackRequest));
            task.setRetryCount(0);
            task.setLastTriedAt(Instant.now());
            rollbackTaskRepository.save(task);
        }



        return orderRepository.save(order);
    }

    @Cacheable(value = "orders", key = "#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Order> getOrdersForUser(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @CacheEvict(value = { "orders", "orderDetails", "orderAuditLogs" }, allEntries = true)
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

    @Cacheable(value = "orderAuditLogs", key = "#orderId")
    public List<OrderAuditLogDTO> getAuditLogsForOrder(Long orderId) {
        List<OrderAuditLog> logs = auditLogRepository.findByOrderIdOrderByCreatedAtDesc(orderId);

        return logs.stream().map(OrderAuditLogDTO::fromEntity).toList();
    }



}
