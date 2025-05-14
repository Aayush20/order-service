package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Item inside the cart")
public class CartItemDTO {

    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID", example = "101")
    private Long productId;

    @NotNull
    @Schema(description = "Product name", example = "iPhone 15")
    private String productName;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity of the product", example = "2")
    private int quantity;

    @NotNull(message = "Unit price is required")
    @Schema(description = "Unit price", example = "999.99")
    private BigDecimal unitPrice;

    @NotNull(message = "Currency is required")
    @Schema(description = "Currency code", example = "USD")
    private String currency;

    public CartItemDTO() {
    }
    public CartItemDTO(Long productId, String productName, int quantity, BigDecimal unitPrice, String currency) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.currency = currency;
    }


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
