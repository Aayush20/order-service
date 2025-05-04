package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.orderservice.models.Order;
import org.example.orderservice.models.OrderItem;
import org.example.orderservice.models.ShippingAddress;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
@Schema(
        name = "OrderResponseDTO",
        description = "Represents a user's order",
        example = """
    {
      "orderId": 101,
      "userId": "user_123",
      "orderDate": "2024-05-04T10:00:00Z",
      "status": "PLACED",
      "shippingAddress": {
        "street": "221B Baker Street",
        "city": "London",
        "state": "Greater London",
        "zipCode": "NW1 6XE"
      },
      "orderItems": [
        {
          "productId": 1005,
          "productName": "iPhone 15",
          "quantity": 2,
          "unitPrice": 999.99,
          "currency": "USD"
        }
      ]
    }
    """
)
//@Schema(description = "Response DTO representing a placed order")
public class OrderResponseDTO {

    @Schema(description = "Unique ID of the order", example = "101")
    private Long orderId;

    @Schema(description = "User ID who placed the order", example = "user_123")
    private String userId;

    @Schema(description = "Date when the order was placed", example = "2024-05-04T10:00:00Z")
    private Date orderDate;

    @Schema(description = "Current status of the order", example = "PLACED")
    private String status;

    private ShippingAddressDTO shippingAddress;

    private List<OrderItemDTO> orderItems;

    public static OrderResponseDTO fromOrder(Order order) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(order.getId());
        response.setUserId(order.getUserId());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus().name());

        // map embedded ShippingAddress
        ShippingAddress sa = order.getShippingAddress();
        ShippingAddressDTO shipping = new ShippingAddressDTO();
        shipping.setStreet(sa.getStreet());
        shipping.setCity(sa.getCity());
        shipping.setState(sa.getState());
        shipping.setZipCode(sa.getZipCode());
        response.setShippingAddress(shipping);

        // map order items
        List<OrderItemDTO> items = order.getOrderItems().stream().map(item -> {
            OrderItemDTO dto = new OrderItemDTO();
            dto.setProductId(item.getProductId());
            dto.setProductName(item.getProductName());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setCurrency(item.getCurrency());
            return dto;
        }).collect(Collectors.toList());

        response.setOrderItems(items);
        return response;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ShippingAddressDTO getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddressDTO shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<OrderItemDTO> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemDTO> orderItems) {
        this.orderItems = orderItems;
    }
}
