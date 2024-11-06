package org.example.paymentservice.dto;

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
    private BigDecimal price;
    private Integer quantity;
    private Integer discount;
    private String stockStatus;
    private BigDecimal weight;
    private Integer categoryId;
    private List<String> imageUrls;

    @Override
    public String toString() {
        return "ProductDTO{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", discount=" + discount +
                ", stockStatus='" + stockStatus + '\'' +
                ", weight=" + weight +
                ", categoryId=" + categoryId +
                ", imageUrls=" + imageUrls +
                '}';
    }

}
