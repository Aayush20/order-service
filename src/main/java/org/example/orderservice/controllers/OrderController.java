package org.example.orderservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.orderservice.dtos.*;
import org.example.orderservice.models.Order;
import org.example.orderservice.services.OrderService;
import org.example.orderservice.services.TokenService;
import org.example.orderservice.utils.TokenClaimUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/order")
@Tag(name = "Order APIs", description = "Operations related to cart and order processing")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TokenService tokenService;

    @Operation(
            summary = "Get current user's cart",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cart fetched",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CartDTO.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                        {
                          "timestamp": "2024-05-04T10:00:00",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "JWT expired or invalid",
                          "path": "/order/cart"
                        }
                        """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/cart")
    public ResponseEntity<CartDTO> showCart(@RequestHeader("Authorization") String tokenHeader) {
        var token = tokenService.introspect(tokenHeader);
        return ResponseEntity.ok(CartDTO.fromEntity(orderService.getCart(token.getSub())));
    }

    @Operation(summary = "Add item to cart",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Item added to cart",
                            content = @Content(schema = @Schema(implementation = CartDTO.class)))
            }
    )
    @PostMapping("/cart")
    public ResponseEntity<CartDTO> addToCart(@RequestHeader("Authorization") String tokenHeader,
                                             @RequestBody @Valid CartItemRequestDTO cartItemRequest) {
        var token = tokenService.introspect(tokenHeader);
        return ResponseEntity.ok(CartDTO.fromEntity(orderService.addToCart(token.getSub(), cartItemRequest)));
    }

    @Operation(summary = "Remove item from cart",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Item removed",
                            content = @Content(schema = @Schema(implementation = CartDTO.class)))
            }
    )
    @DeleteMapping("/cart/{itemId}")
    public ResponseEntity<CartDTO> removeFromCart(@RequestHeader("Authorization") String tokenHeader,
                                                  @PathVariable Long itemId) {
        var token = tokenService.introspect(tokenHeader);
        return ResponseEntity.ok(CartDTO.fromEntity(orderService.removeItemFromCart(token.getSub(), itemId)));
    }

    @Operation(summary = "Place an order",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order placed",
                            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Cart is empty or validation failed",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                    {
                      "timestamp": "2024-05-04T11:35:00",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Cart is empty",
                      "path": "/order/placeorder"
                    }
                    """)
                            )
                    )
            }
    )
    @PostMapping("/placeorder")
    public ResponseEntity<OrderResponseDTO> placeOrder(Authentication authentication,
                                                       @RequestBody @Valid OrderRequestDTO orderRequest,
                                                       @RequestHeader("Authorization") String tokenHeader) throws IOException {
        var token = tokenService.introspect(tokenHeader);
        String strippedToken = tokenHeader.replace("Bearer ", "");
        Order order = orderService.placeOrder(token.getSub(), orderRequest, strippedToken);
        return ResponseEntity.ok(OrderResponseDTO.fromOrder(order));
    }

    @Operation(summary = "Get paginated orders for current user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated order list returned",
                            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class)))
            }
    )
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderResponseDTO>> getOrders(@RequestHeader("Authorization") String tokenHeader,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        var token = tokenService.introspect(tokenHeader);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponseDTO> responsePage = orderService.getOrdersForUser(token.getSub(), pageable)
                .map(OrderResponseDTO::fromOrder);
        return ResponseEntity.ok(responsePage);
    }

    @Operation(summary = "Get specific order by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order found",
                            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Order not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId, @RequestHeader("Authorization") String tokenHeader) {
        var token = tokenService.introspect(tokenHeader);
        Order order = orderService.getOrderByIdAndUserId(orderId, token.getSub());
        return ResponseEntity.ok(OrderResponseDTO.fromOrder(order));
    }

    @Operation(summary = "Cancel an order",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order cancelled",
                            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class)))
            }
    )
    @PutMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long orderId, @RequestHeader("Authorization") String tokenHeader) throws JsonProcessingException {
        var token = tokenService.introspect(tokenHeader);
        Order order = orderService.cancelOrder(orderId, token.getSub());

        return ResponseEntity.ok(OrderResponseDTO.fromOrder(order));
    }

    @Operation(summary = "Admin-only: Update order status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order status updated",
                            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable Long orderId,
                                                              @RequestBody @Valid OrderStatusUpdateRequestDTO request,
                                                              @RequestHeader("Authorization") String tokenHeader) {
        var token = tokenService.introspect(tokenHeader);
        if (!TokenClaimUtils.hasRole(token, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Order order = orderService.updateOrderStatus(orderId, request.getNewStatus(), token.getSub());
        return ResponseEntity.ok(OrderResponseDTO.fromOrder(order));
    }

    @Operation(summary = "Admin-only: Get audit logs for an order",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Audit logs returned",
                            content = @Content(schema = @Schema(implementation = OrderAuditLogDTO.class)))
            }
    )
    @GetMapping("/orders/{orderId}/audit-log")
    public ResponseEntity<List<OrderAuditLogDTO>> getOrderAuditLog(@PathVariable Long orderId,
                                                                   @RequestHeader("Authorization") String tokenHeader) {
        var token = tokenService.introspect(tokenHeader);
        if (!TokenClaimUtils.hasRole(token, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(orderService.getAuditLogsForOrder(orderId));
    }

    @Operation(summary = "Get current JWT claims (debug only)",
            description = "Returns the authenticated user's JWT claims")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentTokenInfo(@RequestHeader("Authorization") String tokenHeader) {
        return ResponseEntity.ok(tokenService.introspect(tokenHeader));
    }
}
