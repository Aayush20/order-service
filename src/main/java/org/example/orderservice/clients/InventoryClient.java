package org.example.orderservice.clients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.example.orderservice.dtos.RollbackStockRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class InventoryClient {

    private static final Logger logger = LoggerFactory.getLogger(InventoryClient.class);
    private final RestTemplate restTemplate;

    public InventoryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retry(name = "productService", fallbackMethod = "getProductFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public void rollbackStock(RollbackStockRequestDto request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RollbackStockRequestDto> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity("http://prod-cat-service/internal/rollback-stock", entity, Void.class);
            logger.info("✅ Rolled back stock for products: {}", request.getProducts());
        } catch (HttpStatusCodeException ex) {
            logger.error("❗ Failed to rollback stock. Status: {}, Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("Rollback API failed", ex);
        } catch (Exception ex) {
            logger.error("❗ Failed to rollback stock: {}", ex.getMessage());
            throw new RuntimeException("Rollback API failed", ex);
        }
    }
}
