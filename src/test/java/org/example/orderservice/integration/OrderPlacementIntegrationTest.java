package org.example.orderservice.integration;

import org.example.orderservice.dtos.CartItemDTO;
import org.example.orderservice.dtos.ShippingAddressDTO;
import org.example.orderservice.integration.IntegrationTestConfig;
import org.example.orderservice.dtos.OrderRequestDTO;

import org.example.orderservice.models.Order;
import org.example.orderservice.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderPlacementIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldPlaceOrderAndSaveToDatabase() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(123L); // or "user123" if userId is String in your model

        CartItemDTO item = new CartItemDTO(1L, "iPhone", 2, new BigDecimal("999.99"), "INR");
        ShippingAddressDTO address = new ShippingAddressDTO("123 St", "City", "State", "123456");

        request.setShippingAddress(address);
        request.setCartItems(List.of(item));

        ResponseEntity<String> response = restTemplate.postForEntity("/order/place", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
