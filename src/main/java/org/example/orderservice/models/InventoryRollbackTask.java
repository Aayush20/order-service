package org.example.orderservice.models;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "inventory_rollback_queue")
public class InventoryRollbackTask extends BaseEntity {

    @Column(nullable = false)
    private Long orderId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload; // JSON payload of RollbackStockRequestDto

    private int retryCount = 0;

    private boolean success = false;

    private Instant lastTriedAt;

    // Getters and setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Instant getLastTriedAt() {
        return lastTriedAt;
    }

    public void setLastTriedAt(Instant lastTriedAt) {
        this.lastTriedAt = lastTriedAt;
    }
}

