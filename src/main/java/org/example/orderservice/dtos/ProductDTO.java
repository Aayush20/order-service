package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Snapshot of a product fetched from the product catalog")
public class ProductDTO {

    @Schema(description = "Product ID", example = "1005")
    @NotNull
    private Long productId;

    @Schema(description = "Product name", example = "Samsung Galaxy S24")
    @NotBlank
    private String name;

    @Schema(description = "Price of the product", example = "999.99")
    @NotNull
    private BigDecimal price;

    @Schema(description = "Currency code", example = "INR")
    @NotNull
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
