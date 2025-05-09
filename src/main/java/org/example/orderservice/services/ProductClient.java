package org.example.orderservice.services;

import io.github.resilience4j.retry.annotation.Retry;
import org.example.orderservice.dtos.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

    @Retry(name = "productService", fallbackMethod = "fallbackProductDetails")
    public ProductDTO getProductDetails(Long productId) {
        //String url = productServiceUrl + "/products/" + productId;
        String url = "http://prod-cat-service/products/" + productId;
        return restTemplate.getForObject(url, ProductDTO.class);
    }

    // Fallback method in case retries fail
    public ProductDTO fallbackProductDetails(Long productId, Throwable ex) {
        logger.error("❗ Fallback: Unable to fetch product details for id {} after retries. Error: {}", productId, ex.getMessage());
        throw new RuntimeException("Product service is unavailable. Please try again later.");
    }
}
