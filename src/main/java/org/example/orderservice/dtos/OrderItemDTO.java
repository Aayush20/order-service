package org.example.orderservice.dtos;

import java.math.BigDecimal;

public class OrderItemDTO {
    private Long productId;
    private String productName;
    private int quantity;
    // Display price as a decimal value (e.g., 50.00)
    private BigDecimal unitPrice;
    private String currency;

    public OrderItemDTO() { }

    public Long getProductId() {
        return productId;
    }
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * For communication with the Payment Service, convert the display price (e.g., 50.00)
     * into its minor-unit representation (e.g., 5000).
     */
    public Long getUnitPriceInMinorUnits() {
        if(unitPrice == null) return null;
        return unitPrice.multiply(BigDecimal.valueOf(100)).longValue();
    }
}
