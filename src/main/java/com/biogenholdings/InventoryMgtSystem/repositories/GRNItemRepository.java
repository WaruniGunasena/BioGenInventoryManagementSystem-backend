package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.GRNItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface GRNItemRepository extends JpaRepository<GRNItem, Long> {
    List<GRNItem> findByGrnId(Long grnId);
    List<GRNItem> findByGrnIdAndIsDeletedFalse(Long grnId);

    // 3. Expiry Alerts: Items expiring within a specific date
    @Query("SELECT gi.product.name as productName, gi.batchNumber as batch, gi.expDate as expiry, (gi.quantity + gi.bonus) as qty " +
            "FROM GRNItem gi WHERE gi.expDate <= :thresholdDate " +
            "AND gi.isDeleted = false AND gi.quantity > 0 " +
            "ORDER BY gi.expDate ASC")
    List<Map<String, Object>> findUpcomingExpiries(@Param("thresholdDate") LocalDate thresholdDate);
}