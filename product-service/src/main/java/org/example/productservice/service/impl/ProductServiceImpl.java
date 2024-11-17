package org.example.productservice.service.impl;

import org.example.productservice.dto.ProductDTO;
import org.example.productservice.entity.Category;
import org.example.productservice.entity.Product;
import org.example.productservice.repository.CategoryRepository;
import org.example.productservice.repository.ProductRepository;
import org.example.productservice.service.CloudinaryService;
import org.example.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.io.IOException;

import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    private final String uploadDir = "uploads/images/";

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private ProductDTO convertToProductDTO(Product product) {

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

    private Product convertToProduct(ProductDTO productDTO) {
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

    @Override
    public ProductDTO createProductWithImages(ProductDTO productDTO, List<MultipartFile> images) throws IOException {
        Product product = convertToProduct(productDTO);
        List<String> imageUrls = new ArrayList<>();

        if (images != null) {
            for (MultipartFile image : images) {
                String imageUrl = cloudinaryService.uploadImage(image).get("url");
                imageUrls.add(imageUrl);
            }
        }

        product.setImagePathsList(imageUrls);
        productRepository.save(product);
        return convertToProductDTO(product);
    }


    @Override
    public ProductDTO updateProductWithImages(Long productId, ProductDTO productDTO, List<MultipartFile> images) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setProductName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        product.setPriceInput(productDTO.getPriceInput());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());

        if (productDTO.getDiscount() != null) {
            product.setDiscount(productDTO.getDiscount());
        }

        product.setStockStatus(productDTO.getStockStatus());
        product.setWeight(productDTO.getWeight());

        if (productDTO.getCategoryName() != null) {
            Category category = categoryRepository.findCategoryByName(productDTO.getCategoryName())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (images != null && !images.isEmpty()) {
            deleteAllImageUrls(productId);
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : images) {
                String imageUrl = cloudinaryService.uploadImage(file).get("url");
                imageUrls.add(imageUrl);
            }
            product.setImagePathsList(imageUrls);
        }

        productRepository.save(product);
        return convertToProductDTO(product);
    }




    @Override
    public void updatePartialImageUrls(Long productId, List<String> imageUrlsToDelete, List<MultipartFile> newImages) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<String> currentPaths = new ArrayList<>(product.getImagePathsList());

        if (imageUrlsToDelete != null) {
            for (String url : imageUrlsToDelete) {
                try {
                    cloudinaryService.deleteImage(url);
                    currentPaths.remove(url);
                } catch (IOException e) {
                    System.err.println("Error deleting image from Cloudinary: " + e.getMessage());
                }
            }
        }

        if (newImages != null) {
            for (MultipartFile file : newImages) {
                String imageUrl = cloudinaryService.uploadImage(file).get("url");
                currentPaths.add(imageUrl);
            }
        }

        product.setImagePathsList(currentPaths);
        productRepository.save(product);
    }





    @Override
    public List<String> getImageUrls(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return product.getImagePathsList();
    }

    @Override
    public void deleteImageUrls(Long productId, List<String> imageUrlsToDelete) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        List<String> currentUrls = new ArrayList<>(product.getImagePathsList());

        for (String url : imageUrlsToDelete) {
            try {
                cloudinaryService.deleteImage(url);
                currentUrls.remove(url);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete image from Cloudinary: " + url, e);
            }
        }

        product.setImagePathsList(currentUrls);
        productRepository.save(product);
    }


    @Override
    public void deleteAllImageUrls(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        for (String url : product.getImagePathsList()) {
            try {
                cloudinaryService.deleteImage(url);
            } catch (IOException e) {
                System.err.println("Error deleting image from Cloudinary: " + e.getMessage());
            }
        }

        product.setImagePathsList(Collections.emptyList());
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void reduceStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if(product.getQuantity() < quantity){
            throw new RuntimeException( productId + "Not enough quantity stock");
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
    }


    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        try {
            deleteAllImageUrls(id);
        } catch (Exception e) {
            System.err.println("Error deleting all images from Cloudinary for product: " + e.getMessage());
        }

        productRepository.delete(product);
    }



    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        // Truy vấn phân trang từ repository
        Page<Product> productPage = productRepository.findAll(pageable);

        // Chuyển đổi từ Page<Product> sang Page<ProductDTO>
        return productPage.map(this::convertToProductDTO);  // Chuyển đổi các đối tượng Product thành ProductDTO
    }





    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToProductDTO(product);
    }
}
