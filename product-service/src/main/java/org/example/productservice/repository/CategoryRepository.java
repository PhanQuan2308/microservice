package org.example.productservice.repository;

import jakarta.transaction.Transactional;
import org.example.productservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO category (category_name) VALUES (:categoryName)", nativeQuery = true)
    void addCategory(@Param("categoryName") String categoryName);

    @Transactional
    @Modifying
    @Query(value = "UPDATE category SET category_name = :categoryName WHERE category_id = :categoryId", nativeQuery = true)
    void updateCategory(@Param("categoryId") Long categoryId, @Param("categoryName") String categoryName);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM category WHERE category_id = :categoryId", nativeQuery = true)
    void deleteCategory(@Param("categoryId") Long categoryId);

    @Query(value = "SELECT * FROM category WHERE category_id = :categoryId", nativeQuery = true)
    Category getCategoryById(@Param("categoryId") Long categoryId);

    @Query(value = "SELECT * FROM category", nativeQuery = true)
    List<Category> getAllCategories();

    @Query(value = "SELECT * FROM category where category_name = :categoryName", nativeQuery = true)
    Optional<Category> findCategoryByName(@Param("categoryName") String categoryName);

    @Query("SELECT c FROM Category c JOIN FETCH c.products WHERE c.categoryId = :categoryId")
    Category findCategoryWithProducts(Long categoryId);
}
