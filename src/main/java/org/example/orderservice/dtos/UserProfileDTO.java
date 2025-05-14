package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User profile and address details")
public class UserProfileDTO {

    @Schema(description = "Full name of the user", example = "Aayush Kumar")
    private String name;

    @Schema(description = "Email address", example = "aayush@example.com")
    private String email;

    @Schema(description = "User's saved address")
    private AddressDTO address;

    public static class AddressDTO {
        @Schema(description = "Street address", example = "123 Elm Street")
        private String street;

        @Schema(description = "City name", example = "Bangalore")
        private String city;

        @Schema(description = "State name", example = "Karnataka")
        private String state;

        @Schema(description = "Zip or postal code", example = "560001")
        private String zipCode;

        // Getters and Setters
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public AddressDTO getAddress() { return address; }
    public void setAddress(AddressDTO address) { this.address = address; }
}
