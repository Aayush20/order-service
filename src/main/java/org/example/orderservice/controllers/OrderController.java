package org.example.orderservice.controllers;

import org.example.orderservice.dtos.CartItemRequestDTO;
import org.example.orderservice.dtos.OrderRequestDTO;
import org.example.orderservice.dtos.OrderResponseDTO;
import org.example.orderservice.models.Cart;
import org.example.orderservice.models.Order;
import org.example.orderservice.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * GET /order/cart: Returns the current cart for the authenticated user.
     */
    @GetMapping("/cart")
    public ResponseEntity<Cart> showCart(Authentication authentication) {
        String userId = authentication.getName();
        Cart cart = orderService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * POST /order/cart: Adds an item to the user's cart.
     */
    @PostMapping("/cart")
    public ResponseEntity<Cart> addToCart(Authentication authentication,
                                          @RequestBody CartItemRequestDTO cartItemRequest) {
        String userId = authentication.getName();
        Cart cart = orderService.addToCart(userId, cartItemRequest);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /order/cart/{itemId}: Removes an item from the user's cart.
     */
    @DeleteMapping("/cart/{itemId}")
    public ResponseEntity<Cart> removeFromCart(Authentication authentication,
                                               @PathVariable Long itemId) {
        String userId = authentication.getName();
        Cart cart = orderService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * POST /order/placeorder: Places an order based on the user’s cart.
     */
    @PostMapping("/placeorder")
    public ResponseEntity<OrderResponseDTO> placeOrder(Authentication authentication,
                                                       @RequestBody OrderRequestDTO orderRequest) {
        String userId = authentication.getName();
        Order order = orderService.placeOrder(userId, orderRequest);
        OrderResponseDTO response = OrderResponseDTO.fromOrder(order);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /order/orders: Retrieves all orders for the authenticated user.
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> getOrders(Authentication authentication) {
        String userId = authentication.getName();
        List<Order> orders = orderService.getOrdersForUser(userId);
        List<OrderResponseDTO> responses = orders.stream()
                .map(OrderResponseDTO::fromOrder)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
