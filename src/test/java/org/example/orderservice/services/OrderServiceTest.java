package org.example.orderservice.services;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.example.orderservice.clients.InventoryClient;
import org.example.orderservice.clients.ProductClient;
import org.example.orderservice.clients.UserProfileClient;
import org.example.orderservice.dtos.*;
import org.example.orderservice.kafka.KafkaPublisher;
import org.example.orderservice.models.*;
import org.example.orderservice.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;
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
    @Mock private SendGridEmailService emailService;
    @Mock private InventoryClient inventoryClient;
    @Mock private InventoryRollbackTaskRepository rollbackTaskRepository;
    @Mock private UserProfileClient userProfileClient;

    @InjectMocks private OrderService orderService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(
                cartRepository,
                orderRepository,
                productClient,
                kafkaPublisher,
                auditLogRepository,
                new SimpleMeterRegistry(),
                emailService,
                inventoryClient,
                rollbackTaskRepository,
                userProfileClient
        );
    }

    @Test
    void shouldAddToCart_whenValidProductProvided() {
        String userId = "user123";
        Long productId = 1L;
        CartItemRequestDTO req = new CartItemRequestDTO(productId, 2);
        ProductDTO productDTO = new ProductDTO(productId, "Product", BigDecimal.valueOf(999), "INR");

        when(productClient.getProductDetails(productId)).thenReturn(productDTO);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cart result = orderService.addToCart(userId, req);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("Product", result.getItems().get(0).getProductName());
    }

    @Test
    void shouldThrow_whenProductIsUnavailable() {
        String userId = "user123";
        Long productId = 1L;
        CartItemRequestDTO req = new CartItemRequestDTO(productId, 1);
        ProductDTO unavailable = new ProductDTO(productId, "UNAVAILABLE", BigDecimal.ZERO, "NA");

        when(productClient.getProductDetails(productId)).thenReturn(unavailable);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.addToCart(userId, req));

        assertTrue(ex.getMessage().contains("Product is temporarily unavailable"));
    }

    @Test
    void shouldPlaceOrder_whenCartAndUserProfileValid() throws IOException {
        String userId = "user123";
        String token = "token-abc";
        Cart cart = new Cart(userId);
        cart.addItem(new CartItem(1L, 2, "Phone", BigDecimal.valueOf(500), "INR"));

        UserProfileDTO profile = new UserProfileDTO();
        profile.setEmail("user123@example.com");
        UserProfileDTO.AddressDTO address = new UserProfileDTO.AddressDTO();
        address.setCity("Delhi"); address.setStreet("MG Road");
        address.setState("Delhi"); address.setZipCode("110001");
        profile.setAddress(address);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(userProfileClient.getUserProfile(token)).thenReturn(profile);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.placeOrder(userId, new OrderRequestDTO(), token);

        assertNotNull(result);
        assertEquals(OrderStatus.PLACED, result.getStatus());
        verify(emailService).sendEmail(eq("user123@example.com"), any(), any());
        verify(kafkaPublisher).publishOrderPlaced(any());
    }
    @Test
    void shouldThrow_whenCartIsEmptyDuringOrderPlacement() {
        String userId = "user123";
        String token = "mock-token";
        Cart emptyCart = new Cart(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(emptyCart));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.placeOrder(userId, new OrderRequestDTO(), token));

        assertEquals("Cart is empty", ex.getMessage());
    }

    @Test
    void shouldCancelOrder_whenOrderIsPlaced() throws IOException {
        Long orderId = 101L;
        String userId = "user123";

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PLACED);
        order.addOrderItem(new OrderItem(1L, 2));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.cancelOrder(orderId, userId);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(inventoryClient).rollbackStock(any());
        verify(auditLogRepository).save(any());
        verify(rollbackTaskRepository).save(any());
        verify(kafkaPublisher).publishOrderCancelled(order);
    }

    @Test
    void shouldThrow_whenCancellingNonPlacedOrder() {
        Long orderId = 999L;
        String userId = "user123";

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.cancelOrder(orderId, userId));

        assertTrue(ex.getMessage().contains("Only placed orders can be cancelled"));
    }

    @Test
    void shouldUpdateStatusToDelivered_whenCalledByAdmin() {
        Long orderId = 77L;
        String admin = "admin";

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED, admin);

        assertEquals(OrderStatus.DELIVERED, result.getStatus());
        verify(auditLogRepository).save(any());
    }

    @Test
    void shouldThrow_whenTryingToUpdateStatusToShippedManually() {
        Long orderId = 99L;
        String admin = "admin";

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED, admin));

        assertTrue(ex.getMessage().contains("SHIPPED status is set automatically"));
    }

}
