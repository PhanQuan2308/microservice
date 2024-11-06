package org.example.productservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class ProductDTO {
    private Long productId;
    private String productName;
    private String description;
    private BigDecimal priceInput;
    private BigDecimal price;
    private Integer quantity;
    private Integer discount;
    private String stockStatus;
    private String weight;
    private String categoryName;
    private List<String> imageUrls;
}
