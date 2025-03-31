//package org.example.orderservice.services;
//
//import org.example.orderservice.dtos.ProductDTO;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//@Service
//public class ProductClient {
//
//    private final RestTemplate restTemplate;
//
//    @Value("${product.service.url}")
//    private String productServiceUrl;  // Example: http://localhost:8081
//
//    public ProductClient(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    /**
//     * Retrieves product details by product ID.
//     */
//    public ProductDTO getProductDetails(Long productId) {
//        String url = productServiceUrl + "/products/" + productId;
//        return restTemplate.getForObject(url, ProductDTO.class);
//    }
//}
//
//
package org.example.orderservice.services;

import org.example.orderservice.dtos.ProductDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class ProductClient {

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductClient(RestTemplate restTemplate,
                         @Value("${product.service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    public ProductDTO getProductDetails(Long productId) {
        // Check if we're in dummy mode.
        if ("dummy".equalsIgnoreCase(productServiceUrl)) {
            ProductDTO dummyProduct = new ProductDTO();
            dummyProduct.setProductId(productId);
            dummyProduct.setName("Dummy Product");
            // Set display unit price as 50.00 (for order service display).
            dummyProduct.setPrice(BigDecimal.valueOf(50.00));
            dummyProduct.setCurrency("USD");
            return dummyProduct;
        }
        // Otherwise, if a real service URL is provided, proceed with real REST call.
        String url = productServiceUrl + "/products/" + productId;
        return restTemplate.getForObject(url, ProductDTO.class);
    }
}

