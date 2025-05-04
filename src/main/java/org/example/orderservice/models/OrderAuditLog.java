package org.example.orderservice.models;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "order_audit_log")
public class OrderAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private String performedBy;

    private String action;

    private Instant createdAt = Instant.now();

    public OrderAuditLog() {}

    public OrderAuditLog(Long orderId, String performedBy, String action) {
        this.orderId = orderId;
        this.performedBy = performedBy;
        this.action = action;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}