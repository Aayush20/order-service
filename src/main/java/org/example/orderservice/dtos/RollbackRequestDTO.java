package org.example.orderservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request for rolling back product stock")
public class RollbackRequestDTO {

    @Schema(description = "List of product IDs to rollback", example = "[101, 102, 103]")
    @NotEmpty
    @Size(min = 1, message = "At least one product ID must be provided")
    private List<Long> productIds;

    public RollbackRequestDTO() {}

    public RollbackRequestDTO(List<Long> productIds) {
        this.productIds = productIds;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}
