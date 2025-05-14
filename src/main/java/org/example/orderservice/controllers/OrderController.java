package org.example.orderservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.orderservice.dtos.*;
import org.example.orderservice.models.Order;
import org.example.orderservice.models.OrderStatus;
import org.example.orderservice.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/order")
@Tag(name = "Order APIs", description = "Operations related to cart and order processing")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Get current user's cart",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cart fetched",
                            content = @Content(schema = @Schema(implementation = CartDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping("/cart")
    public ResponseEntity<CartDTO> showCart(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(CartDTO.fromEntity(orderService.getCart(userId)));
    }

    @Operation(summary = "Add item to cart",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Item added",
                            content = @Content(schema = @Schema(implementation = CartDTO.class)))
            })
    @PostMapping("/cart")
    public ResponseEntity<CartDTO> addToCart(Authentication authentication,
                                             @RequestBody @Valid CartItemRequestDTO cartItemRequest) {
        String userId = authentication.getName();
        return ResponseEntity.ok(CartDTO.fromEntity(orderService.addToCart(userId, cartItemRequest)));
    }

    @Operation(summary = "Remove item from cart",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Item removed",
                            content = @Content(schema = @Schema(implementation = CartDTO.class)))
            })
    @DeleteMapping("/cart/{itemId}")
    public ResponseEntity<CartDTO> removeFromCart(Authentication authentication,
                                                  @PathVariable Long itemId) {
        String userId = authentication.getName();
        return ResponseEntity.ok(CartDTO.fromEntity(orderService.removeItemFromCart(userId, itemId)));
    }

    @Operation(summary = "Place an order",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order placed",
                            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Cart is empty or invalid request",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping("/placeorder")
    public ResponseEntity<OrderResponseDTO> placeOrder(Authentication authentication,
                                                       @RequestBody @Valid OrderRequestDTO orderRequest,
                                                       @RequestHeader("Authorization") String authHeader) {
        String userId = authentication.getName();
        String token = authHeader.replace("Bearer ", "");
        Order order = orderService.placeOrder(userId, orderRequest, token);
        return ResponseEntity.ok(OrderResponseDTO.fromOrder(order));
    }

    @Operation(summary = "Get paginated orders for current user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated order list",
                            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class)))
            })
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderResponseDTO>> getOrders(Authentication authentication,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponseDTO> responsePage = orderService.getOrdersForUser(userId, pageable)
                .map(OrderResponseDTO::fromOrder);
        return ResponseEntity.ok(responsePage);
    }

    @Operation(summary = "Get order by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order details",
                            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Order not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId, Authentication authentication) {
        String userId = authentication.getName();
        Order order = orderService.getOrderByIdAndUserId(orderId, userId);
        return ResponseEntity.ok(OrderResponseDTO.fromOrder(order));
    }

    @Operation(summary = "Cancel an order by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order cancelled",
                            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class)))
            })
    @PutMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        String userId = authentication.getName();
        Order order = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(OrderResponseDTO.fromOrder(order));
    }

    @Operation(summary = "Update order status (Admin only)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order status updated",
                            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable Long orderId,
                                                              @RequestBody @Valid OrderStatusUpdateRequestDTO request,
                                                              Authentication authentication) {
        String userId = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Order order = orderService.updateOrderStatus(orderId, request.getNewStatus(), userId);
        return ResponseEntity.ok(OrderResponseDTO.fromOrder(order));
    }

    @Operation(summary = "Get audit logs for an order (Admin only)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Audit logs returned",
                            content = @Content(schema = @Schema(implementation = OrderAuditLogDTO.class)))
            })
    @GetMapping("/orders/{orderId}/audit-log")
    public ResponseEntity<List<OrderAuditLogDTO>> getOrderAuditLog(@PathVariable Long orderId, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(orderService.getAuditLogsForOrder(orderId));
    }
}
