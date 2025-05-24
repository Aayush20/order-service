
package org.example.orderservice.clients;

import org.example.orderservice.dtos.ProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductClient productClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productClient = new ProductClient(restTemplate, "http://prod-cat-service");
    }

    @Test
    void shouldReturnProductDetailsFromService() {
        ProductDTO mockProduct = new ProductDTO(1L, "Test Product", BigDecimal.valueOf(100), "INR");
        when(restTemplate.getForObject("http://prod-cat-service/products/1", ProductDTO.class))
                .thenReturn(mockProduct);

        ProductDTO result = productClient.getProductDetails(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.getCurrency()).isEqualTo("INR");
    }

    @Test
    void shouldReturnFallbackProductWhenErrorOccurs() {
        Throwable ex = new RuntimeException("Simulated failure");

        ProductDTO fallback = productClient.getProductFallback(99L, ex);

        assertThat(fallback).isNotNull();
        assertThat(fallback.getProductId()).isEqualTo(99L);
        assertThat(fallback.getName()).isEqualTo("UNAVAILABLE");
        assertThat(fallback.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(fallback.getCurrency()).isEqualTo("N/A");
    }
}
