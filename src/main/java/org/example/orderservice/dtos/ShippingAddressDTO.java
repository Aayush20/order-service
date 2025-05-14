package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Shipping address details")
public class ShippingAddressDTO {

    @NotBlank(message = "Street is required")
    @Schema(example = "221B Baker Street")
    private String street;

    @NotBlank(message = "City is required")
    @Schema(example = "London")
    private String city;

    @NotBlank(message = "State is required")
    @Schema(example = "Greater London")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Schema(example = "NW1 6XE")
    private String zipCode;

    public ShippingAddressDTO() {
    }

    public ShippingAddressDTO(String street, String city, String state, String zipCode) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }



    public @NotBlank(message = "Street is required") String getStreet() {
        return street;
    }

    public void setStreet(@NotBlank(message = "Street is required") String street) {
        this.street = street;
    }

    public @NotBlank(message = "City is required") String getCity() {
        return city;
    }

    public void setCity(@NotBlank(message = "City is required") String city) {
        this.city = city;
    }

    public @NotBlank(message = "State is required") String getState() {
        return state;
    }

    public void setState(@NotBlank(message = "State is required") String state) {
        this.state = state;
    }

    public @NotBlank(message = "Postal code is required") String getZipCode() {
        return zipCode;
    }

    public void setZipCode(@NotBlank(message = "Postal code is required") String zipCode) {
        this.zipCode = zipCode;
    }
}
