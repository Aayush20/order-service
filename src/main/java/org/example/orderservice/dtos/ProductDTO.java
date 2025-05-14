package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Snapshot of a product fetched from the product catalog")
public class ProductDTO {

    @Schema(example = "1005")
    private Long productId;

    @Schema(example = "Samsung Galaxy S24")
    private String name;

    @Schema(example = "999.99")
    private BigDecimal price;

    @Schema(example = "INR")
    private String currency;

    public ProductDTO() {
    }
    public ProductDTO(Long productId, String name, BigDecimal price, String currency) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.currency = currency;
    }


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
