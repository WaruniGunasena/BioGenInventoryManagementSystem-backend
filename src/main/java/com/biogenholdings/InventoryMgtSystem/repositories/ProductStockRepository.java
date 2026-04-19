package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.dtos.StockResponseDTO;
import com.biogenholdings.InventoryMgtSystem.models.ProductStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {


    @Query("SELECT ps FROM ProductStock ps WHERE ps.product.id IN :productIds")
    List<ProductStock> findByProductIdIn(@Param("productIds") List<Long> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ps FROM ProductStock ps WHERE ps.product.id = :productId")
    Optional<ProductStock> findByProductIdForUpdate(@Param("productId") Long productId);

    Optional<ProductStock> findByProductId(Long productId);

    @Query("""
        SELECT new com.biogenholdings.InventoryMgtSystem.dtos.StockResponseDTO(
            ps.id,
            p.id,
            p.name,
            ps.totalQuantity,
            ps.sellingPrice,
            p.minimumStockLevel,
            p.reorderLevel,
            p.itemCode,
            p.packSize
        )
        FROM ProductStock ps
        JOIN ps.product p
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchKey, '%'))
    """)
    Page<StockResponseDTO> searchStockByNamePaginated(Pageable pageable,@Param("searchKey") String searchKey);

    @Query("""
        SELECT new com.biogenholdings.InventoryMgtSystem.dtos.StockResponseDTO(
            ps.id,
            p.id,
            p.name,
            ps.totalQuantity,
            ps.sellingPrice,
            p.minimumStockLevel,
            p.reorderLevel,
            p.itemCode,
            p.packSize
        )
        FROM ProductStock ps
        JOIN ps.product p
    """)
    Page<StockResponseDTO> getPaginatedStockData(Pageable pageable);

    @Query("""
        SELECT new com.biogenholdings.InventoryMgtSystem.dtos.StockResponseDTO(
            ps.id,
            p.id,
            p.name,
            ps.totalQuantity,
            ps.sellingPrice,
            p.minimumStockLevel,
            p.reorderLevel,
            p.itemCode,
            p.packSize
        )
        FROM ProductStock ps
        JOIN ps.product p
    """)
    List<StockResponseDTO> getAllStockData();
}