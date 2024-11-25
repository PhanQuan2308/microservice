package org.example.productservice.mapper;

import org.example.productservice.dto.ProductDTO;
import org.example.productservice.entity.Category;
import org.example.productservice.entity.Product;
import org.example.productservice.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    @Autowired
    private CategoryRepository categoryRepository;

    public ProductDTO convertToProductDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(product.getProductId());
        productDTO.setProductName(product.getProductName());
        productDTO.setDescription(product.getDescription());
        productDTO.setPriceInput(product.getPriceInput());
        productDTO.setPrice(product.getPrice());
        productDTO.setQuantity(product.getQuantity());
        productDTO.setDiscount(product.getDiscount());
        productDTO.setStockStatus(product.getStockStatus());
        productDTO.setWeight(product.getWeight());

        if (product.getCategory() != null) {
            productDTO.setCategoryName(product.getCategory().getCategoryName());
        }

        productDTO.setImageUrls(product.getImagePathsList());
        return productDTO;
    }

    public Product convertToProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setProductId(productDTO.getProductId());
        product.setProductName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        product.setPriceInput(productDTO.getPriceInput());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());
        product.setDiscount(productDTO.getDiscount());
        product.setStockStatus(productDTO.getStockStatus());
        product.setWeight(productDTO.getWeight());

        Category category = categoryRepository.findCategoryByName(productDTO.getCategoryName())
                .orElseThrow(() -> new RuntimeException("Category with name " + productDTO.getCategoryName() + " not found"));
        product.setCategory(category);

        product.setImagePathsList(productDTO.getImageUrls());
        return product;
    }
}
