package org.example.productservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.example.productservice.dto.ProductDTO;
import org.example.productservice.event.ProductStockReductionRequest;
import org.example.productservice.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @PostMapping("/with-images")
    public ResponseEntity<ProductDTO> createProductWithImages(
            @Valid @ModelAttribute ProductDTO productDTO,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
            if (images == null) images = new ArrayList<>();
            ProductDTO createdProduct = productService.createProductWithImages(productDTO, images);
            return ResponseEntity.ok(createdProduct);
        } catch (IOException e) {
            logger.error("Error creating product with images", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/reduce-stock")
    public ResponseEntity<String> reduceStock(
            @RequestBody List<ProductStockReductionRequest> stockReductionRequests) {
        try {
            productService.reduceStock(stockReductionRequests);
            return ResponseEntity.ok("Stock reduced successfully for all requested products");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to reduce stock: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDTO> productsPage = productService.getAllProducts(pageable);
            return ResponseEntity.ok(productsPage);
        } catch (Exception e) {
            logger.error("Error retrieving products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    @GetMapping("/{productId}/images")
    public ResponseEntity<List<String>> getProductImages(@PathVariable Long productId) {
        List<String> imageUrls = productService.getImageUrls(productId);
        return ResponseEntity.ok(imageUrls);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        try {
            ProductDTO product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            logger.error("Error retrieving product with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @PutMapping("/{productId}/images/partial")
    public ResponseEntity<String> updatePartialProductImages(
            @PathVariable Long productId,
            @RequestParam("imageUrlsToDelete") String imageUrlsToDeleteJson,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages) {
        try {
            List<String> imageUrlsToDelete = new ObjectMapper().readValue(imageUrlsToDeleteJson, new TypeReference<List<String>>() {});
            productService.updatePartialImageUrls(productId, imageUrlsToDelete, newImages);
            return ResponseEntity.ok("Product images updated successfully");
        } catch (IOException e) {
            logger.error("Error updating partial images", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating partial images");
        }
    }

    @DeleteMapping("/{productId}/images")
    public ResponseEntity<String> deleteProductImages(
            @PathVariable Long productId,
            @RequestBody List<String> imageUrls) {
        try {
            productService.deleteImageUrls(productId, imageUrls);
            return ResponseEntity.ok("Product images deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting images for Product ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting product images");
        }
    }

    @DeleteMapping("/{productId}/images/all")
    public ResponseEntity<String> deleteAllProductImages(@PathVariable Long productId) {
        try {
            productService.deleteAllImageUrls(productId);
            return ResponseEntity.ok("All product images deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting all images for Product ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting all product images");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (RuntimeException e) {
            logger.error("Error deleting product with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to delete product: " + e.getMessage());
        }
    }


    @PutMapping("/{productId}/with-images")
    public ResponseEntity<ProductDTO> updateProductWithImages(
            @PathVariable Long productId,
            @Valid @ModelAttribute ProductDTO productDTO,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
            ProductDTO updatedProduct = productService.updateProductWithImages(productId, productDTO, images);
            return ResponseEntity.ok(updatedProduct);
        } catch (IOException e) {
            logger.error("Error updating product with images", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
