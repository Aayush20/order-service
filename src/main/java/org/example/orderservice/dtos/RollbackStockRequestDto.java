package org.example.orderservice.dtos;

import java.util.List;

public class RollbackStockRequestDto {
    private List<ProductRollbackEntry> products;
    private String reason;

    public RollbackStockRequestDto() {}

    public RollbackStockRequestDto(List<ProductRollbackEntry> products, String reason) {
        this.products = products;
        this.reason = reason;
    }

    public List<ProductRollbackEntry> getProducts() {
        return products;
    }

    public void setProducts(List<ProductRollbackEntry> products) {
        this.products = products;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public static class ProductRollbackEntry {
        private Long productId;
        private int quantity;

        public ProductRollbackEntry() {}

        public ProductRollbackEntry(Long productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
