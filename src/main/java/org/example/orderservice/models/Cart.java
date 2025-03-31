package org.example.orderservice.models;

import jakarta.persistence.*;


import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
public class Cart extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public Cart() { }

    public Cart(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public List<CartItem> getItems() {
        return items;
    }
    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public void addItem(CartItem item) {
        item.setCart(this);
        items.add(item);
    }
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }
}
