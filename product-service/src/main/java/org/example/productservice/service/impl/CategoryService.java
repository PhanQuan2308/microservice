package org.example.productservice.service.impl;

import org.example.productservice.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CategoryService {
    Category addCategory(String categoryName);
    Category updateCategory(Long categoryId, String categoryName);
    void deleteCategory(Long categoryId);
    Category getCategoryById(Long categoryId);
    List<Category> getAllCategories();
}
