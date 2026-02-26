package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    Optional<ProductStock> findByProductIdAndBatchNumber(Long productId, String batchNumber);
}