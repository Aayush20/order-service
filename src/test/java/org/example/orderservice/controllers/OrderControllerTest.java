package org.example.orderservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.dtos.*;
import org.example.orderservice.models.*;
import org.example.orderservice.services.OrderService;
import org.example.orderservice.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(OrderControllerTest.TestConfig.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private OrderService orderService;
    @Autowired private TokenService tokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String token = "Bearer abc";

    @TestConfiguration
    static class TestConfig {
        @Bean public OrderService orderService() { return Mockito.mock(OrderService.class); }
        @Bean public TokenService tokenService() { return Mockito.mock(TokenService.class); }
    }

    @BeforeEach
    void setupMocks() {
        TokenIntrospectionResponseDTO tokenDto = new TokenIntrospectionResponseDTO();
        tokenDto.setSub("user123");
        when(tokenService.introspect(token)).thenReturn(tokenDto);

        Cart cart = new Cart("user123");
        when(orderService.getCart("user123")).thenReturn(cart);
    }

    @Test
    void shouldReturnCartForAuthorizedUser() throws Exception {
        mockMvc.perform(get("/order/cart").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.items").exists());
    }

    @Test
    void shouldFailWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/order/cart"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldPlaceOrderSuccessfully() throws Exception {
        CartItemDTO itemDTO = new CartItemDTO(1L, "iPhone", 2, BigDecimal.valueOf(100000), "INR");

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(123L);
        ShippingAddressDTO address = new ShippingAddressDTO("123 St", "City", "State", "123456");
        request.setShippingAddress(address);
        request.setCartItems(List.of(itemDTO));

        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setUserId("user123");

        when(orderService.placeOrder(eq("user123"), any(OrderRequestDTO.class), eq(token)))
                .thenReturn(mockOrder);

        mockMvc.perform(post("/order/place")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void shouldCancelOrderSuccessfully() throws Exception {
        Order order = new Order();
        order.setId(42L);
        order.setStatus(OrderStatus.CANCELLED);
        order.setUserId("user123");

        when(orderService.cancelOrder(42L, "user123")).thenReturn(order);

        mockMvc.perform(patch("/order/orders/42/cancel").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldUpdateStatusToDeliveredAsAdmin() throws Exception {
        TokenIntrospectionResponseDTO tokenDto = new TokenIntrospectionResponseDTO();
        tokenDto.setSub("admin");
        tokenDto.setRoles(List.of("ADMIN"));
        when(tokenService.introspect(token)).thenReturn(tokenDto);

        Order order = new Order();
        order.setId(55L);
        order.setStatus(OrderStatus.DELIVERED);

        when(orderService.updateOrderStatus(eq(55L), eq(OrderStatus.DELIVERED), eq("admin"))).thenReturn(order);

        mockMvc.perform(patch("/order/orders/55/status")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "newStatus": "DELIVERED"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void shouldReturnOrderDetailsForUser() throws Exception {
        Long orderId = 42L;
        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setUserId("user123");
        mockOrder.setStatus(OrderStatus.PLACED);
        mockOrder.setOrderDate(new Date());

        ShippingAddress sa = new ShippingAddress("123 St", "City", "State", "123456");
        mockOrder.setShippingAddress(sa);
        mockOrder.setOrderItems(List.of(new OrderItem(1L, 2, "Product", BigDecimal.valueOf(1999), "INR")));

        when(orderService.getOrderByIdAndUserId(orderId, "user123")).thenReturn(mockOrder);

        mockMvc.perform(get("/order/orders/" + orderId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.orderItems").isArray());
    }

    @Test
    void shouldReturnPaginatedOrdersForUser() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user123");
        order.setStatus(OrderStatus.PLACED);

        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderService.getOrdersForUser(eq("user123"), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/order/orders/me")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value("user123"))
                .andExpect(jsonPath("$.content[0].status").value("PLACED"));
    }

    @Test
    void shouldReturnAuditLogsForAdmin() throws Exception {
        TokenIntrospectionResponseDTO tokenDto = new TokenIntrospectionResponseDTO();
        tokenDto.setSub("adminUser");
        tokenDto.setRoles(List.of("ADMIN"));
        when(tokenService.introspect(token)).thenReturn(tokenDto);

        OrderAuditLogDTO log1 = new OrderAuditLogDTO("adminUser", "ORDER_PLACED", Instant.now());
        OrderAuditLogDTO log2 = new OrderAuditLogDTO("system", "ORDER_SHIPPED", Instant.now());

        when(orderService.getAuditLogsForOrder(42L)).thenReturn(List.of(log1, log2));

        mockMvc.perform(get("/order/audit/42").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].performedBy").value("adminUser"))
                .andExpect(jsonPath("$[1].action").value("ORDER_SHIPPED"));
    }

}
