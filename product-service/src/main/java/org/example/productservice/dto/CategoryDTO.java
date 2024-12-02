package org.example.productservice.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryDTO {
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
