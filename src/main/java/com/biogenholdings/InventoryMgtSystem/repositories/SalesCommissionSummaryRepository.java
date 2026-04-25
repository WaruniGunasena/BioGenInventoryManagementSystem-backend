package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.SalesCommissionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SalesCommissionSummaryRepository extends JpaRepository<SalesCommissionSummary, Long> {

    @Query(value = "SELECT SUM(s.total_commission) FROM sales_commission_summary s " +
            "WHERE s.sales_rep_id = :repId " +
            "AND s.invoice_date LIKE CONCAT(:monthYear, '%')",
            nativeQuery = true)
    BigDecimal getMonthlyTotalForRep(@Param("repId") Long repId, @Param("monthYear") String monthYear);

    @Query("SELECT s FROM SalesCommissionSummary s " +
            "LEFT JOIN FETCH s.customer " +
            "WHERE s.salesRepId = :salesRepId " + // Changed s.salesRep.id to s.salesRepId
            "ORDER BY s.invoiceDate DESC")
    List<SalesCommissionSummary> findBySalesRepId(@Param("salesRepId") Long salesRepId);

}
