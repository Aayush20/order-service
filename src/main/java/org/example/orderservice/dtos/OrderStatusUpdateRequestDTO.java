package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.example.orderservice.models.OrderStatus;

@Schema(description = "Request to update the status of an order")
public class OrderStatusUpdateRequestDTO {

    @NotNull(message = "New status must not be null")
    @Schema(description = "New status for the order", example = "DELIVERED")
    private OrderStatus newStatus;

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OrderStatus newStatus) {
        this.newStatus = newStatus;
    }
}
