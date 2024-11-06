package org.example.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetailsDTO {
    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private double price;
    private ProductDTO product;
}
