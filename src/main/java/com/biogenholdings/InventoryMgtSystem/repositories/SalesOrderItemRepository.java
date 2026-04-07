package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.SalesOrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {

    @Query("SELECT i.product.name as name, SUM(i.quantity) as totalQty " +
            "FROM SalesOrderItem i " +
            "GROUP BY i.product.id, i.product.name " +
            "ORDER BY totalQty DESC")
    List<Map<String, Object>> findTopSellingProducts(Pageable pageable);
}
