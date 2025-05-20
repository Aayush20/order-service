package org.example.orderservice.clients;

import org.example.orderservice.dtos.ProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductClientCircuitBreakerTest {

    private ProductClient productClient;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate(); // Not mocked to let it fail
        productClient = new ProductClient(restTemplate, "http://localhost:9999"); // Invalid URL
    }

    @Test
    void shouldTriggerFallbackAfterCircuitBreakerOpens() {
        ProductDTO result = null;
        for (int i = 0; i < 5; i++) {
            try {
                result = productClient.getProductDetails(1L);
            } catch (Exception ignored) {
            }
        }

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("UNAVAILABLE");
    }
}
