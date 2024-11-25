package org.example.productservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Entity
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private String productName;
    private String description;
    private BigDecimal priceInput;
    private BigDecimal price;
    private Integer quantity;

    @Column(nullable = true)
    private Integer discount;

    private String stockStatus;
    private String weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String imagePaths;

    public List<String> getImagePathsList() {
        return imagePaths != null ? Arrays.asList(imagePaths.split(",")) : List.of();
    }

    public void setImagePathsList(List<String> imagePathsList) {
        this.imagePaths = (imagePathsList != null) ? String.join(",", imagePathsList) : "";
    }



}
