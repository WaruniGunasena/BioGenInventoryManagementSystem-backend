package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.dtos.StockResponseDTO;
import com.biogenholdings.InventoryMgtSystem.models.ProductStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    Optional<ProductStock> findByProductId(Long productId);

    @Query("""
        SELECT new com.biogenholdings.InventoryMgtSystem.dtos.StockResponseDTO(
            ps.id,
            p.id,
            p.name,
            ps.totalQuantity,
            ps.sellingPrice,
            p.minimumStockLevel,
            p.reorderLevel
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
            p.reorderLevel
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
            p.reorderLevel
        )
        FROM ProductStock ps
        JOIN ps.product p
    """)
    List<StockResponseDTO> getAllStockData();
}