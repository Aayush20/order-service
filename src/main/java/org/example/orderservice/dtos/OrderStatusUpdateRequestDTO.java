package org.example.orderservice.dtos;

import jakarta.validation.constraints.NotNull;
import org.example.orderservice.models.OrderStatus;

public class OrderStatusUpdateRequestDTO {

    @NotNull(message = "New status must not be null")
    private OrderStatus newStatus;

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OrderStatus newStatus) {
        this.newStatus = newStatus;
    }
}
