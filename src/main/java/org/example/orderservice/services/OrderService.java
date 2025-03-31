package org.example.orderservice.services;

import org.example.orderservice.dtos.CartItemRequestDTO;
import org.example.orderservice.dtos.OrderRequestDTO;
import org.example.orderservice.models.Cart;
import org.example.orderservice.models.CartItem;
import org.example.orderservice.models.Order;
import org.example.orderservice.models.OrderItem;
import org.example.orderservice.models.ShippingAddress;
import org.example.orderservice.repositories.CartRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

    /**
     * Adds an item to the cart.
     * Retrieves current product details from the Product Catalog Service
     * to populate the cart item snapshot (e.g. product name, unit price, currency).
     */
    @Transactional
    public Cart addToCart(String userId, CartItemRequestDTO cartItemRequest) {
        // Fetch product snapshot
        var product = productClient.getProductDetails(cartItemRequest.getProductId());

        // Get the user's existing cart, or create a new one
        Cart cart = cartRepository.findByUserId(userId).orElse(new Cart(userId));

        CartItem cartItem = new CartItem(cartItemRequest.getProductId(), cartItemRequest.getQuantity());
        cartItem.setProductName(product.getName());
        cartItem.setUnitPrice(product.getPrice());
        cartItem.setCurrency(product.getCurrency());

        cart.addItem(cartItem);
        return cartRepository.save(cart);
    }

    /**
     * Returns the current cart for the specified user.
     */
    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId).orElse(new Cart(userId));
    }

    /**
     * Removes a specific item from the user’s cart.
     */
    @Transactional
    public Cart removeItemFromCart(String userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        return cartRepository.save(cart);
    }

    /**
     * Places an order from the user’s cart.
     * The shippingAddress in the OrderRequest is mapped to the Order's Embedded ShippingAddress.
     * Order items are built by copying the cart item snapshot.
     */
    @Transactional
    public Order placeOrder(String userId, OrderRequestDTO orderRequest) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        // Map DTO shipping address to model ShippingAddress
        ShippingAddress shippingAddress = new ShippingAddress(
                orderRequest.getShippingAddress().getStreet(),
                orderRequest.getShippingAddress().getCity(),
                orderRequest.getShippingAddress().getState(),
                orderRequest.getShippingAddress().getZipCode()
        );
        Order order = new Order(userId, shippingAddress);

        // Build order items from cart items (using stored snapshots)
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(cartItem.getProductId(), cartItem.getQuantity());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setCurrency(cartItem.getCurrency());
            order.addOrderItem(orderItem);
        }
        Order savedOrder = orderRepository.save(order);
        // Clear the cart after a successful order placement
        cartRepository.delete(cart);
        return savedOrder;
    }

    /**
     * Fetches all orders for the user.
     */
    public List<Order> getOrdersForUser(String userId) {
        return orderRepository.findByUserId(userId);
    }
}
