package org.example.orderservice.dtos;

import org.example.orderservice.models.OrderAuditLog;

import java.time.Instant;

public class OrderAuditLogDTO {
    private String performedBy;
    private String action;
    private Instant timestamp;

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