package org.example.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class ProductDTO {
    private Long productId;

    @NotBlank(message = "Product name is not blank")
    private String productName;

    private String description;

    @NotNull(message = "Price input can not be null")
    private BigDecimal priceInput;

    @NotNull(message = "Price can not be null")
    private BigDecimal price;

    @NotNull(message = "Quantity can not be null")
    private Integer quantity;

    private Integer discount;

    @NotBlank(message = "Stock status is not blank")
    private String stockStatus;

    private String weight;

    private String categoryName;
    private List<String> imageUrls;
}
