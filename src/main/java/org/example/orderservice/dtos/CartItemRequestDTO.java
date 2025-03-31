package org.example.orderservice.dtos;

public class CartItemRequestDTO {
    private Long productId;
    private int quantity;

    public CartItemRequestDTO() { }

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

