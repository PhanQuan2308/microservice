package org.example.productservice.service.impl;

import org.example.productservice.dto.ProductDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface ProductService {
    List<ProductDTO> getAllProducts();
    ProductDTO getProductById(Long id);
    void deleteProduct(Long id);

    ProductDTO createProductWithImages(ProductDTO productDTO, List<MultipartFile> images) throws IOException;

    ProductDTO updateProductWithImages(Long productId, ProductDTO productDTO, List<MultipartFile> images) throws IOException;

    void updatePartialImageUrls(Long productId, List<String> imageUrlsToDelete, List<MultipartFile> newImages) throws IOException;

    List<String> getImageUrls(Long productId);

    void deleteImageUrls(Long productId, List<String> imageUrlsToDelete);

    void deleteAllImageUrls(Long productId);
}
