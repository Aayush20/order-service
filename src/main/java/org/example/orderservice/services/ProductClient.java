package org.example.orderservice.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.example.orderservice.dtos.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductClient.class);

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductClient(RestTemplate restTemplate,
                         @Value("${product.service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    @Retry(name = "productService", fallbackMethod = "getProductFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public ProductDTO getProductDetails(Long productId) {
        //String url = productServiceUrl + "/products/" + productId;
        String url = "http://prod-cat-service/products/" + productId;
        return restTemplate.getForObject(url, ProductDTO.class);
    }

    // Fallback after retries/circuit breaker
    public ProductDTO getProductFallback(Long productId, Throwable ex) {
        logger.error("❗ ProductClient fallback triggered for productId={} - {}", productId, ex.getMessage(), ex);

        ProductDTO fallbackProduct = new ProductDTO();
        fallbackProduct.setProductId(productId);
        fallbackProduct.setName("UNAVAILABLE");
        fallbackProduct.setPrice(BigDecimal.valueOf(0.0));
        fallbackProduct.setCurrency("N/A");

        return fallbackProduct;
    }
}
