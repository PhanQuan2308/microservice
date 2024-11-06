package org.example.productservice.service;

import org.example.productservice.dto.ProductDTO;
import org.example.productservice.entity.Category;
import org.example.productservice.entity.Product;
import org.example.productservice.repository.CategoryRepository;
import org.example.productservice.repository.ProductRepository;
import org.example.productservice.service.impl.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final String uploadDir = "uploads/images/";

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

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
                String imageUrl = saveImageToServer(image);
                imageUrls.add(imageUrl);
            }
        }

        product.setImagePathsList(imageUrls);
        productRepository.save(product);
        return convertToProductDTO(product);
    }


    private String saveImageToServer(MultipartFile image) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, image.getBytes());
        return "/uploads/images/" + fileName;
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
        product.setDiscount(productDTO.getDiscount());
        product.setStockStatus(productDTO.getStockStatus());
        product.setWeight(productDTO.getWeight());
        if (productDTO.getCategoryName() != null) {
            Category category = categoryRepository.findCategoryByName(productDTO.getCategoryName())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (images != null && !images.isEmpty()) {
            deleteAllImageUrls(productId);

            List<String> imagePaths = new ArrayList<>();
            Files.createDirectories(Paths.get(uploadDir));

            for (MultipartFile file : images) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, file.getBytes());
                imagePaths.add(filePath.toString());
            }
            product.setImagePathsList(imagePaths);
        }

        productRepository.save(product);
        return convertToProductDTO(product);
    }


    @Override
    public void updatePartialImageUrls(Long productId, List<String> imageUrlsToDelete, List<MultipartFile> newImages) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        List<String> currentPaths = new ArrayList<>(product.getImagePathsList());

        currentPaths.removeAll(imageUrlsToDelete);

        Files.createDirectories(Paths.get(uploadDir));
        for (MultipartFile file : newImages) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, file.getBytes());
            currentPaths.add(filePath.toString());
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
        currentUrls.removeAll(imageUrlsToDelete);
        product.setImagePathsList(currentUrls);
        productRepository.save(product);
    }

    @Override
    public void deleteAllImageUrls(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setImagePaths(null);
        productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductDTO> productDTOs = new ArrayList<>();
        for (Product product : products) {
            productDTOs.add(convertToProductDTO(product));
        }
        return productDTOs;
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToProductDTO(product);
    }
}
