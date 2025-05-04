//package org.example.orderservice.controllers;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.example.orderservice.dtos.*;
//import org.example.orderservice.models.Order;
//import org.example.orderservice.models.OrderStatus;
//import org.example.orderservice.models.ShippingAddress;
//import org.example.orderservice.services.OrderService;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Collections;
//import java.util.Date;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SuppressWarnings("removal")
//@WebMvcTest(OrderController.class)
//@Import(OrderController.class)  // Add this if needed depending on Spring Boot version
//public class OrderControllerTest {
//
//    @Autowired private MockMvc mockMvc;
//    @Autowired private ObjectMapper objectMapper;
//
//    @MockBean private OrderService orderService;
//
//    @Test
//    @WithMockUser(username = "user123")
//    void shouldReturnCartForUser() throws Exception {
//        Mockito.when(orderService.getCart("user123"))
//                .thenReturn(new org.example.orderservice.models.Cart("user123"));
//
//        mockMvc.perform(get("/order/cart"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @WithMockUser(username = "user123")
//    void shouldPlaceOrderSuccessfully() throws Exception {
//        // Prepare request
//        OrderRequestDTO request = new OrderRequestDTO();
//        ShippingAddressDTO shipping = new ShippingAddressDTO();
//        shipping.setStreet("Street");
//        shipping.setCity("City");
//        shipping.setState("State");
//        shipping.setZipCode("123456");
//        request.setShippingAddress(shipping);
//        request.setUserId(100L);  // Not actually used in logic
//        request.setCartItems(Collections.emptyList());
//
//        // Mock order return
//        Order mockOrder = new Order("user123", new ShippingAddress("Street", "City", "State", "123456"));
//        mockOrder.setId(1L);
//        mockOrder.setStatus(OrderStatus.PLACED);
//        mockOrder.setOrderDate(new Date());
//
//        Mockito.when(orderService.placeOrder(any(), any())).thenReturn(mockOrder);
//
//        // Perform request
//        mockMvc.perform(post("/order/placeorder")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.orderId").value(1));
//    }
//
//    @Test
//    @WithMockUser(username = "user123")
//    void shouldCancelOrderSuccessfully() throws Exception {
//        Order mockOrder = new Order("user123", new ShippingAddress("A", "B", "C", "D"));
//        mockOrder.setId(1L);
//        mockOrder.setStatus(OrderStatus.CANCELLED);
//        mockOrder.setOrderDate(new Date());
//
//        Mockito.when(orderService.cancelOrder(1L, "user123")).thenReturn(mockOrder);
//
//        mockMvc.perform(put("/order/orders/1/cancel"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("CANCELLED"));
//    }
//
//    @Test
//    @WithMockUser(username = "admin123", roles = {"USER"}) // Not ADMIN
//    void shouldDenyAdminOnlyStatusUpdate() throws Exception {
//        OrderStatusUpdateRequestDTO request = new OrderStatusUpdateRequestDTO();
//        request.setNewStatus(OrderStatus.DELIVERED);
//
//        mockMvc.perform(put("/order/orders/1/status")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isForbidden());
//    }
//}
