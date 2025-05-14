package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Represents an individual item in an order")
public class OrderItemDTO {

    @Schema(description = "Product ID", example = "2001")
    private Long productId;

    @Schema(description = "Product name", example = "AirPods Pro")
    private String productName;

    @Schema(description = "Quantity ordered", example = "2")
    private int quantity;

    @Schema(description = "Price per item", example = "249.99")
    private BigDecimal unitPrice;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    public Long getUnitPriceInMinorUnits() {
        if (unitPrice == null) return null;
        return unitPrice.multiply(BigDecimal.valueOf(100)).longValue();
    }
    public OrderItemDTO() {
    }

    public OrderItemDTO(Long productId, String productName, int quantity, BigDecimal unitPrice, String currency) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.currency = currency;
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
