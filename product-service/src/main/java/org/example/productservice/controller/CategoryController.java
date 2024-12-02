package org.example.productservice.controller;

import org.example.productservice.dto.CategoryDTO;
import org.example.productservice.dto.ProductDTO;
import org.example.productservice.entity.Category;
import org.example.productservice.entity.Product;
import org.example.productservice.mapper.CategoryMapper;
import org.example.productservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryMapper categoryMapper;

    @PostMapping("/create")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody Category category) {
        Category createdCategory = categoryService.addCategory(category.getCategoryName());
        CategoryDTO categoryDTO = categoryMapper.toDTO(createdCategory);
        return ResponseEntity.ok(categoryDTO);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long categoryId, @RequestBody String categoryName) {
        Category updatedCategory = categoryService.updateCategory(categoryId, categoryName);
        CategoryDTO categoryDTO = categoryMapper.toDTO(updatedCategory);
        return ResponseEntity.ok(categoryDTO);
    }


    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long categoryId) {
        CategoryDTO categoryDTO = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(categoryDTO);
    }


    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryId}/products")
    public ResponseEntity<List<ProductDTO>> getProductsByCategoryId(@PathVariable Long categoryId) {
        List<ProductDTO> products = categoryService.getProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

}
