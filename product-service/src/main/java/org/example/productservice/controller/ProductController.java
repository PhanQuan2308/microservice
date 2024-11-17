package org.example.productservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.example.productservice.dto.ProductDTO;
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
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/reduce-stock")
    public ResponseEntity<Void> reduceStock(
            @RequestParam Long productId,
            @RequestParam Integer quantity
    ){
        productService.reduceStock(productId, quantity);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> productsPage = productService.getAllProducts(pageable);
        return ResponseEntity.ok(productsPage);
    }




    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{productId}/images/partial")
    public ResponseEntity<Void> updatePartialProductImages(
            @PathVariable Long productId,
            @RequestParam("imageUrlsToDelete") String imageUrlsToDeleteJson,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages) {
        try {
            List<String> imageUrlsToDelete = new ObjectMapper().readValue(imageUrlsToDeleteJson, new TypeReference<List<String>>() {});
            productService.updatePartialImageUrls(productId, imageUrlsToDelete, newImages);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            logger.error("Error updating partial images", e);
            return ResponseEntity.status(500).build();
        }
    }




    @GetMapping("/{productId}/images")
    public ResponseEntity<List<String>> getProductImages(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getImageUrls(productId));
    }

    @DeleteMapping("/{productId}/images")
    public ResponseEntity<Void> deleteProductImages(
            @PathVariable Long productId,
            @RequestBody List<String> imageUrls) {
        productService.deleteImageUrls(productId, imageUrls);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}/images/all")
    public ResponseEntity<Void> deleteAllProductImages(@PathVariable Long productId) {
        productService.deleteAllImageUrls(productId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{productId}/with-images")
    public ResponseEntity<ProductDTO> updateProductWithImages(
            @PathVariable Long productId,
            @Valid  @ModelAttribute ProductDTO productDTO,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
            ProductDTO updatedProduct = productService.updateProductWithImages(productId, productDTO, images);
            return ResponseEntity.ok(updatedProduct);
        } catch (IOException e) {
            logger.error("Error updating product with images", e);
            return ResponseEntity.status(500).build();
        }
    }

}
