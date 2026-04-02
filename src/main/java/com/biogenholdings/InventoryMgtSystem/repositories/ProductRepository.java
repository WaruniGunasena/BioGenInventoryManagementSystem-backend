package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    //List<Product> findByNameContainingOrDescriptionContaining(String name, String description);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND (p.name LIKE %:searchKey% OR p.description LIKE %:searchKey%)")
    List<Product> searchActiveProducts(@Param("searchKey") String searchKey);

    Page<Product> findByIsDeletedFalse(Pageable pageable);
    List<Product> findByIsDeletedFalse(Sort sort);
    Page<Product> findByCategoryIdAndIsDeletedFalse(Long categoryID,Pageable pageable);
    List<Product> findByCategoryIdAndIsDeletedFalse(Long categoryID);

    @Query("""
        SELECT p FROM Product p
        WHERE p.isDeleted = false
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (
            :searchKey IS NULL OR :searchKey = '' OR
            LOWER(p.name) LIKE LOWER(CONCAT('%', :searchKey, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchKey, '%'))
        )
        """)
            Page<Product> filterProducts(
                    @Param("searchKey") String searchKey,
                    @Param("categoryId") Long categoryId,
                    Pageable pageable
            );
}
