package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "DTO for placing an order")
public class OrderRequestDTO {

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user placing the order", example = "123")
    private Long userId;

    @Valid
    @NotNull(message = "Shipping address is required")
    @Schema(description = "Shipping address for the order")
    private ShippingAddressDTO shippingAddress;

    @NotEmpty(message = "Cart items must not be empty")
    @Schema(description = "List of items in the cart")
    private List<@Valid CartItemDTO> cartItems;

    public @NotNull(message = "User ID is required") Long getUserId() {
        return userId;
    }

    public void setUserId(@NotNull(message = "User ID is required") Long userId) {
        this.userId = userId;
    }

    public @Valid @NotNull(message = "Shipping address is required") ShippingAddressDTO getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(@Valid @NotNull(message = "Shipping address is required") ShippingAddressDTO shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public @NotEmpty(message = "Cart items must not be empty") List<@Valid CartItemDTO> getCartItems() {
        return cartItems;
    }

    public void setCartItems(@NotEmpty(message = "Cart items must not be empty") List<@Valid CartItemDTO> cartItems) {
        this.cartItems = cartItems;
    }


}
