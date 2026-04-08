package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.Product;
import com.biogenholdings.InventoryMgtSystem.services.StockStatusProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

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

    // 1. Low Stock Alerts: Compare stock total vs reorder level
    @Query("SELECT p.name as name, ps.totalQuantity as currentStock, p.reorderLevel as limit " +
            "FROM Product p JOIN p.productStock ps " +
            "WHERE ps.totalQuantity <= p.reorderLevel AND p.isDeleted = false")
    List<Map<String, Object>> findLowStockProducts();

    // 2. Stock Status Summary (For Pie Chart)
    @Query("SELECT " +
            "SUM(CASE WHEN ps.totalQuantity = 0 THEN 1 ELSE 0 END) AS outOfStock, " +
            "SUM(CASE WHEN ps.totalQuantity > 0 AND ps.totalQuantity <= p.reorderLevel THEN 1 ELSE 0 END) AS lowStock, " +
            "SUM(CASE WHEN ps.totalQuantity > p.reorderLevel THEN 1 ELSE 0 END) AS healthy " +
            "FROM Product p JOIN p.productStock ps WHERE p.isDeleted = false")
    StockStatusProjection getStockStatusCounts();
}
