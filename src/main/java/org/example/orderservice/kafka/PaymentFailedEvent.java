package org.example.orderservice.kafka;


public class PaymentFailedEvent {
    private String orderId;
    private String userId;
    private String provider;
    private String failureReason;
    private long timestamp;

    public PaymentFailedEvent() {}

    public PaymentFailedEvent(String orderId, String userId, String provider, String failureReason, long timestamp) {
        this.orderId = orderId;
        this.userId = userId;
        this.provider = provider;
        this.failureReason = failureReason;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

