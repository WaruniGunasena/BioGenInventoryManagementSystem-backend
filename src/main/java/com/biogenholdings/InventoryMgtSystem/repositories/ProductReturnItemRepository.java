package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.ProductReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<ProductReturnItem> findTopByProductReturn_Customer_IdAndProduct_IdOrderByProductReturn_ReturnDateDesc(Long customerId, Long itemId);

    @Query("SELECT SUM(pri.quantity) FROM ProductReturnItem pri " +
            "WHERE pri.product.id = :productId " +
            "AND pri.productReturn.salesOrder.id = :orderId " +
            "AND pri.productReturn.status = 'Pending' " +
            "AND pri.productReturn.isDeleted = false")
    Integer sumQtyBySalesOrderAndProductAndStatus(
            @Param("orderId") Long orderId,
            @Param("productId") Long productId
    );
}
