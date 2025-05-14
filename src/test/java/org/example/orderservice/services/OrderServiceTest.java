package org.example.orderservice.services;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.example.orderservice.configs.kafka.KafkaPublisher;
import org.example.orderservice.dtos.*;
import org.example.orderservice.models.*;
import org.example.orderservice.repositories.CartRepository;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
import org.example.orderservice.repositories.OrderAuditLogRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ProductClient productClient;
    @Mock private KafkaPublisher kafkaPublisher;
    @Mock private OrderAuditLogRepository auditLogRepository;
    @Mock private EmailService emailService;
    @Mock private InventoryClient inventoryClient;
    @Mock private InventoryRollbackTaskRepository rollbackTaskRepository;
    @Mock private UserProfileClient userProfileClient;

    @InjectMocks private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(
                cartRepository, orderRepository, productClient,
                kafkaPublisher, auditLogRepository,
                new SimpleMeterRegistry(), emailService,
                inventoryClient, rollbackTaskRepository, userProfileClient
        );
    }

    @Test
    void shouldAddItemToCartSuccessfully() {
        String userId = "user123";
        Long productId = 1L;
        CartItemRequestDTO request = new CartItemRequestDTO(productId, 2);

        ProductDTO product = new ProductDTO(productId, "Mock Product", BigDecimal.valueOf(100), "INR");

        when(productClient.getProductDetails(productId)).thenReturn(product);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cart result = orderService.addToCart(userId, request);

        assertNotNull(result);
        assertThat(result.getItems()).hasSize(1);
        assertEquals("Mock Product", result.getItems().get(0).getProductName());
    }

    @Test
    void shouldThrowWhenProductIsUnavailable() {
        String userId = "user123";
        Long productId = 1L;
        CartItemRequestDTO request = new CartItemRequestDTO(productId, 1);

        ProductDTO fallback = new ProductDTO(productId, "UNAVAILABLE", BigDecimal.ZERO, "N/A");
        when(productClient.getProductDetails(productId)).thenReturn(fallback);

        Exception ex = assertThrows(RuntimeException.class, () ->
                orderService.addToCart(userId, request)
        );

        assertTrue(ex.getMessage().contains("Product is temporarily unavailable"));
    }

    @Test
    void shouldPlaceOrderSuccessfullyWithUserProfile() {
        String userId = "user123";
        String token = "mock-token";

        Cart cart = new Cart(userId);
        cart.addItem(new CartItem(1L, 2, "Product", BigDecimal.valueOf(100), "INR"));

        OrderRequestDTO request = new OrderRequestDTO(); // address not required from request anymore

        UserProfileDTO profile = new UserProfileDTO();
        profile.setEmail("user123@example.com");
        UserProfileDTO.AddressDTO addressDTO = new UserProfileDTO.AddressDTO();
        addressDTO.setStreet("123 Street");
        addressDTO.setCity("City");
        addressDTO.setState("State");
        addressDTO.setZipCode("123456");
        profile.setAddress(addressDTO);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(userProfileClient.getUserProfile(token)).thenReturn(profile);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.placeOrder(userId, request, token);

        assertNotNull(result);
        assertEquals(1, result.getOrderItems().size());
        verify(kafkaPublisher).publishOrderPlaced(any());
        verify(auditLogRepository).save(any());
        verify(emailService).sendOrderConfirmationEmail(eq("user123@example.com"), any(), any());
    }

    @Test
    void shouldThrowWhenPlacingOrderWithEmptyCart() {
        String userId = "user123";
        String token = "token";
        Cart emptyCart = new Cart(userId);
        OrderRequestDTO request = new OrderRequestDTO();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(emptyCart));

        Exception ex = assertThrows(RuntimeException.class, () ->
                orderService.placeOrder(userId, request, token)
        );

        assertEquals("Cart is empty", ex.getMessage());
    }

    @Test
    void shouldCancelOrder() {
        String userId = "user123";
        Long orderId = 42L;

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PLACED);
        order.setOrderItems(List.of(new OrderItem(1L, 2)));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        Order result = orderService.cancelOrder(orderId, userId);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(kafkaPublisher).publishOrderCancelled(order);
        verify(auditLogRepository).save(any());
        verify(inventoryClient).rollbackStock(any());
        verify(rollbackTaskRepository).save(any());
    }

    @Test
    void shouldThrowIfCancellingNonPlacedOrder() {
        String userId = "user123";
        Long orderId = 42L;

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Exception ex = assertThrows(RuntimeException.class, () ->
                orderService.cancelOrder(orderId, userId)
        );

        assertTrue(ex.getMessage().contains("Only placed orders can be cancelled"));
    }

    @Test
    void shouldUpdateOrderStatusToDelivered() {
        Long orderId = 100L;
        String adminUser = "admin";

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PLACED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED, adminUser);

        assertEquals(OrderStatus.DELIVERED, result.getStatus());
        verify(auditLogRepository).save(any());
    }

    @Test
    void shouldThrowWhenUpdatingToShippedManually() {
        Long orderId = 100L;
        String adminUser = "admin";

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PLACED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Exception ex = assertThrows(RuntimeException.class, () ->
                orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED, adminUser)
        );

        assertTrue(ex.getMessage().contains("SHIPPED status is set automatically"));
    }
}
