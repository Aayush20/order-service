package org.example.orderservice.services;

import org.example.orderservice.dtos.RollbackRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InventoryClient {

    private static final Logger logger = LoggerFactory.getLogger(InventoryClient.class);
    private final RestTemplate restTemplate;

    public InventoryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void rollbackStock(RollbackRequestDTO request) {
        try {
            restTemplate.postForEntity("http://prod-cat-service/internal/inventory/rollback", request, Void.class);
            logger.info("✅ Rolled back stock for products: {}", request.getProductIds());
        } catch (Exception ex) {
            logger.error("❗ Failed to rollback stock for {}: {}", request.getProductIds(), ex.getMessage());
            // Optional: persist to DB for retry
        }
    }
}
