package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Represents an individual item in an order")
public class OrderItemDTO {

    @Schema(example = "2001")
    private Long productId;

    @Schema(example = "AirPods Pro")
    private String productName;

    @Schema(example = "2")
    private int quantity;

    @Schema(example = "249.99")
    private BigDecimal unitPrice;

    @Schema(example = "USD")
    private String currency;

    public Long getUnitPriceInMinorUnits() {
        if (unitPrice == null) return null;
        return unitPrice.multiply(BigDecimal.valueOf(100)).longValue();
    }

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

}
