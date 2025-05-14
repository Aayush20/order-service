package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.orderservice.models.OrderAuditLog;

import java.time.Instant;

@Schema(description = "Log of actions taken on an order")
public class OrderAuditLogDTO {

    @Schema(description = "Who performed the action", example = "order-service")
    private String performedBy;

    @Schema(description = "Audit log action", example = "ORDER_PLACED")
    private String action;

    @Schema(description = "Timestamp of the action", example = "2024-05-04T12:00:00Z")
    private Instant timestamp;

    public OrderAuditLogDTO() {
    }

    public OrderAuditLogDTO(String performedBy, String action, Instant timestamp) {
        this.performedBy = performedBy;
        this.action = action;
        this.timestamp = timestamp;
    }



    public static OrderAuditLogDTO fromEntity(OrderAuditLog log) {
        return new OrderAuditLogDTO(log.getPerformedBy(), log.getAction(), log.getCreatedAt());
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}