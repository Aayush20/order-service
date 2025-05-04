package org.example.orderservice.services;

import org.example.orderservice.configs.kafka.KafkaPublisher;
import org.example.orderservice.dtos.CartItemRequestDTO;
import org.example.orderservice.dtos.OrderRequestDTO;
import org.example.orderservice.dtos.ProductDTO;
import org.example.orderservice.dtos.ShippingAddressDTO;
import org.example.orderservice.models.*;
import org.example.orderservice.repositories.CartRepository;
import org.example.orderservice.repositories.OrderAuditLogRepository;
import org.example.orderservice.repositories.OrderRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ProductClient productClient;
    @Mock private KafkaPublisher kafkaPublisher;
    @Mock private OrderAuditLogRepository auditLogRepository;
    @Mock private MeterRegistry meterRegistry;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldAddItemToCart() {
        // Given
        String userId = "user123";
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(1L);
        request.setQuantity(2);

        ProductDTO product = new ProductDTO();
        product.setProductId(1L);
        product.setName("Mock Product");
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setCurrency("INR");

        Cart cart = new Cart(userId);

        when(productClient.getProductDetails(1L)).thenReturn(product);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Cart updatedCart = orderService.addToCart(userId, request);

        // Then
        assertThat(updatedCart.getItems()).hasSize(1);
        assertThat(updatedCart.getItems().get(0).getProductName()).isEqualTo("Mock Product");
    }

    @Test
    void shouldPlaceOrderSuccessfully() {
        String userId = "user123";
        Cart cart = new Cart(userId);
        CartItem item = new CartItem(1L, 2);
        item.setProductName("Product");
        item.setCurrency("INR");
        item.setUnitPrice(BigDecimal.valueOf(100.0));
        cart.addItem(item);

        ShippingAddressDTO addr = new ShippingAddressDTO();
        addr.setStreet("Street");
        addr.setCity("City");
        addr.setState("State");
        addr.setZipCode("123456");

        OrderRequestDTO request = new OrderRequestDTO();
        request.setShippingAddress(addr);
        request.setUserId(123L); // dummy ID, not used in logic
        request.setCartItems(Collections.emptyList()); // ignored in current service logic

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.placeOrder(userId, request);

        assertThat(order.getOrderItems()).hasSize(1);
        verify(kafkaPublisher).publishOrderPlaced(any());
        verify(auditLogRepository).save(any());
    }

    @Test
    void shouldThrowWhenCartIsEmpty() {
        String userId = "user123";
        Cart emptyCart = new Cart(userId);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(emptyCart));

        ShippingAddressDTO addr = new ShippingAddressDTO();
        addr.setStreet("Street");
        addr.setCity("City");
        addr.setState("State");
        addr.setZipCode("123456");

        OrderRequestDTO request = new OrderRequestDTO();
        request.setShippingAddress(addr);
        request.setUserId(123L);
        request.setCartItems(Collections.emptyList());

        Exception ex = assertThrows(RuntimeException.class, () -> {
            orderService.placeOrder(userId, request);
        });

        assertThat(ex.getMessage()).isEqualTo("Cart is empty");
    }
}
