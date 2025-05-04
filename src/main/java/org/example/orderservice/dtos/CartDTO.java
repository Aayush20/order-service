package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.orderservice.models.Cart;
import org.example.orderservice.models.CartItem;

import java.util.List;
import java.util.stream.Collectors;

@Schema(
        name = "CartDTO",
        description = "User's cart with product items",
        example = """
    {
      "userId": "user123",
      "items": [
        {
          "productId": 101,
          "productName": "iPhone 15",
          "quantity": 2,
          "unitPrice": 999.99,
          "currency": "USD"
        }
      ]
    }
    """
)
public class CartDTO {

    @Schema(description = "User ID", example = "user123")
    private String userId;

    @Schema(description = "Items in the cart")
    private List<CartItemDTO> items;

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public List<CartItemDTO> getItems() {
        return items;
    }
    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }

    // ✅ Static factory method for mapping
    public static CartDTO fromEntity(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setUserId(cart.getUserId());

        List<CartItemDTO> itemDTOs = cart.getItems().stream().map(item -> {
            CartItemDTO i = new CartItemDTO();
            i.setProductId(item.getProductId());
            i.setProductName(item.getProductName());
            i.setQuantity(item.getQuantity());
            i.setUnitPrice(item.getUnitPrice());
            i.setCurrency(item.getCurrency());
            return i;
        }).collect(Collectors.toList());

        dto.setItems(itemDTOs);
        return dto;
    }
}
