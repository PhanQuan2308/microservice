package org.example.productservice.mapper;

import org.example.productservice.dto.CategoryDTO;
import org.example.productservice.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }

}
