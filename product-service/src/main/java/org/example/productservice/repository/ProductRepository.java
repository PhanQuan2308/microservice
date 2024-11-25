package org.example.productservice.repository;
import org.example.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAll(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId")
    List<Product> findProductsByCategoryId(Long categoryId);
}
