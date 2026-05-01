package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.SalesCommissionSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SalesCommissionSummaryRepository extends JpaRepository<SalesCommissionSummary, Long> {

    @Query(value = "SELECT SUM(s.total_commission) FROM sales_commission_summary s " +
            "WHERE s.sales_rep_id = :repId " +
            "AND s.invoice_date LIKE CONCAT(:monthYear, '%')",
            nativeQuery = true)
    BigDecimal getMonthlyTotalForRep(@Param("repId") Long repId, @Param("monthYear") String monthYear);

    @Query("SELECT s FROM SalesCommissionSummary s " +
            "LEFT JOIN FETCH s.customer " +
            "WHERE s.salesRepId = :salesRepId " +
            "AND s.invoiceDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.invoiceDate DESC")
    Page<SalesCommissionSummary> findBySalesRepIdAndDateRange(
            @Param("salesRepId") Long salesRepId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT SUM(s.TotalCommission) FROM SalesCommissionSummary s " +
            "WHERE s.salesRepId = :userId " +
            "AND s.invoiceDate BETWEEN :start AND :end")
    BigDecimal sumTotalCommissionBySalesRepIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT s FROM SalesCommissionSummary s " +
            "WHERE s.salesRepId = :repId " +
            "AND FUNCTION('DATE_FORMAT', s.invoiceDate, '%Y-%m') = :monthYear " +
            "ORDER BY s.invoiceDate DESC")
    List<SalesCommissionSummary> findByRepAndMonth(
            @Param("repId") Long repId,
            @Param("monthYear") String monthYear
    );
}
