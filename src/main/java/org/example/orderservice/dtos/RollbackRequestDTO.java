package org.example.orderservice.dtos;

import java.util.List;

public class RollbackRequestDTO {
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
