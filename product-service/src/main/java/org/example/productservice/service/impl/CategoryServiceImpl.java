package org.example.productservice.service.impl;

import org.example.productservice.dto.CategoryDTO;
import org.example.productservice.dto.ProductDTO;
import org.example.productservice.entity.Category;
import org.example.productservice.entity.Product;
import org.example.productservice.mapper.CategoryMapper;
import org.example.productservice.mapper.ProductMapper;
import org.example.productservice.producer.CategoryQueueProducer;
import org.example.productservice.repository.CategoryRepository;
import org.example.productservice.repository.ProductRepository;
import org.example.productservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryQueueProducer categoryQueueProducer;

    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Category addCategory(String categoryName) {
        if (categoryRepository.findCategoryByName(categoryName).isPresent()) {
            throw new RuntimeException("Category with name " + categoryName + " already exists");
        }

        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setUpdatedAt(LocalDateTime.now());
        Category savedCategory = categoryRepository.save(category);
        categoryQueueProducer.sendToCategoryQueue(savedCategory);
        return savedCategory;
    }


    @Override
    public Category updateCategory(Long categoryId, String categoryName) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setCategoryName(categoryName);
        category.setUpdatedAt(LocalDateTime.now());
        Category updatedCategory = categoryRepository.save(category);
        categoryQueueProducer.sendToCategoryQueue(updatedCategory);
        return updatedCategory;
    }

    @Override
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found");
        }

        categoryRepository.deleteById(categoryId);
        redisTemplate.delete("category:" + categoryId);
    }

    @Override
    public CategoryDTO getCategoryById(Long categoryId) {
        String key = "category:" + categoryId;
        CategoryDTO categoryDTO = (CategoryDTO) redisTemplate.opsForValue().get(key);
        if (categoryDTO != null) {
            System.out.println("Fetched category from Redis: " + categoryDTO.getCategoryName());
            return categoryDTO;
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categoryDTO = categoryMapper.toDTO(category);
        redisTemplate.opsForValue().set(key, categoryDTO);
        return categoryDTO;
    }


    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        String cacheKey = "category:" + categoryId + ":products";

        List<ProductDTO> productDTOs = (List<ProductDTO>) redisTemplate.opsForValue().get(cacheKey);

        if (productDTOs != null) {
            System.out.println("Fetched products from Redis for category ID: " + categoryId);
            return productDTOs;
        }

        List<Product> products = productRepository.findProductsByCategoryId(categoryId);
        if (products.isEmpty()) {
            throw new RuntimeException("No products found for category ID: " + categoryId);
        }

        productDTOs = products.stream()
                .map(productMapper::convertToProductDTO)
                .toList();

        redisTemplate.opsForValue().set(cacheKey, productDTOs, 10, TimeUnit.MINUTES);
        System.out.printf("Cached products in Redis for category ID: %s\n", categoryId);

        return productDTOs;
    }

    @Override
    public void batchUpdateCategories(List<Category> categories) {
        Map<String, Object> categoryCache = new HashMap<>();
        try {
            for (Category category : categories) {
                String redisKey = "category:" + category.getCategoryId();

                CategoryDTO categoryDTO = categoryMapper.toDTO(category);

                CategoryDTO cachedCategoryDTO = (CategoryDTO) redisTemplate.opsForValue().get(redisKey);
                if (cachedCategoryDTO == null ||
                        (category.getUpdatedAt() != null && cachedCategoryDTO.getUpdatedAt() != null &&
                                category.getUpdatedAt().isAfter(cachedCategoryDTO.getUpdatedAt()))) {
                    categoryCache.put(redisKey, categoryDTO);
                    System.out.printf("Updated category: %s in batch\n", category.getCategoryName());
                }
            }
            if (!categoryCache.isEmpty()) {
                redisTemplate.opsForValue().multiSet(categoryCache);
                System.out.printf("Batch updated categories in Redis\n");
            } else {
                System.out.printf("No categories found in batch\n");
            }
        } catch (Exception e) {
            System.err.printf("Error updating Redis: %s\n", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void updateAllCategoriesToRedis() {
        List<Category> categories = categoryRepository.findAllWithProducts();
        batchUpdateCategories(categories);
    }

}