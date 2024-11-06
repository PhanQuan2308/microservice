package org.example.orderservice.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
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

    @Override
    public String toString() {
        return "ProductDTO{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", priceInput=" + priceInput +
                ", price=" + price +
                ", quantity=" + quantity +
                ", discount=" + discount +
                ", stockStatus='" + stockStatus + '\'' +
                ", weight='" + weight + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", imageUrls=" + imageUrls +
                '}';
    }
}
