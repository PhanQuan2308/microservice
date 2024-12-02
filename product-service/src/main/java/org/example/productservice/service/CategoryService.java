package org.example.productservice.service;

import org.example.productservice.dto.CategoryDTO;
import org.example.productservice.dto.ProductDTO;
import org.example.productservice.entity.Category;
import org.example.productservice.entity.Product;

import java.util.List;

public interface CategoryService {
    Category addCategory(String categoryName);
    Category updateCategory(Long categoryId, String categoryName);
    void deleteCategory(Long categoryId);
    CategoryDTO getCategoryById(Long categoryId);
    List<Category> getAllCategories();
    List<ProductDTO> getProductsByCategoryId(Long categoryId);
    void batchUpdateCategories(List<Category> categories);
    void updateAllCategoriesToRedis();
}
