package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.ProductReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductReturnItemRepository extends JpaRepository<ProductReturnItem, Long> {
    List<ProductReturnItem> findByProductReturn_Customer_IdAndQuantityRemainingToReissueGreaterThan(
            Long customerId,
            Integer minQty
    );
    List<ProductReturnItem> findByProductReturn_Customer_IdAndProduct_IdAndQuantityRemainingToReissueGreaterThan(
            Long customerId,
            Long productId,
            Integer minQty
    );
}
