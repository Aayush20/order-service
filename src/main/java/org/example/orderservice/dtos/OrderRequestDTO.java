package org.example.orderservice.dtos;


import org.example.orderservice.models.ShippingAddress;

public class OrderRequestDTO {
    // For this example, we assume the shipping address is mandatory.
    private ShippingAddress shippingAddress;

    public OrderRequestDTO() { }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}

