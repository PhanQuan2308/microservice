package org.example.productservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockReductionRequest {
    private Long productId;
    private Integer quantity;
}
