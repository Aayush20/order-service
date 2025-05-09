package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Represents an item inside the cart")
public class CartItemDTO {

    @NotNull(message = "Product ID is required")
    @Schema(example = "101")
    private Long productId;

    @NotNull
    @Schema(example = "iPhone 15")
    private String productName;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(example = "2")
    private int quantity;

    @NotNull(message = "Unit price is required")
    @Schema(example = "499.99")
    private BigDecimal unitPrice;

    @NotNull(message = "Currency is required")
    @Schema(example = "USD")
    private String currency;

    public @NotNull(message = "Product ID is required") Long getProductId() {
        return productId;
    }

    public void setProductId(@NotNull(message = "Product ID is required") Long productId) {
        this.productId = productId;
    }

    public @NotNull String getProductName() {
        return productName;
    }

    public void setProductName(@NotNull String productName) {
        this.productName = productName;
    }

    @Min(value = 1, message = "Quantity must be at least 1")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(@Min(value = 1, message = "Quantity must be at least 1") int quantity) {
        this.quantity = quantity;
    }

    public @NotNull(message = "Unit price is required") BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(@NotNull(message = "Unit price is required") BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public @NotNull(message = "Currency is required") String getCurrency() {
        return currency;
    }

    public void setCurrency(@NotNull(message = "Currency is required") String currency) {
        this.currency = currency;
    }

}
