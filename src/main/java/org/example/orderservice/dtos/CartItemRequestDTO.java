package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to add a product to cart")
public class CartItemRequestDTO {

    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID", example = "1001")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity to add", example = "3")
    private int quantity;

    public CartItemRequestDTO() {
    }

    public CartItemRequestDTO(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }


    public @NotNull(message = "Product ID is required") Long getProductId() {
        return productId;
    }

    public void setProductId(@NotNull(message = "Product ID is required") Long productId) {
        this.productId = productId;
    }

    @Min(value = 1, message = "Quantity must be at least 1")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(@Min(value = 1, message = "Quantity must be at least 1") int quantity) {
        this.quantity = quantity;
    }


}
