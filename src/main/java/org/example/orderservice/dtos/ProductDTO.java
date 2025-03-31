package org.example.orderservice.dtos;

import java.math.BigDecimal;

public class ProductDTO {
    private Long productId;
    private String name;
    private BigDecimal price;  // Display price (e.g., 50.00)
    private String currency;

    public ProductDTO() { }

    public Long getProductId() {
        return productId;
    }
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
