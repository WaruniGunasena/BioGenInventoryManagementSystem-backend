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
import java.util.Optional;

public interface SalesCommissionSummaryRepository extends JpaRepository<SalesCommissionSummary, Long> {

    // For the Sales Rep Dashboard: Get their specific commissions
    List<SalesCommissionSummary> findBySalesRepId(Long salesRepId);

    @Query(value = "SELECT SUM(s.total_commission) FROM sales_commission_summary s " +
            "WHERE s.sales_rep_id = :repId " +
            "AND s.invoice_date LIKE CONCAT(:monthYear, '%')",
            nativeQuery = true)
    BigDecimal getMonthlyTotalForRep(@Param("repId") Long repId, @Param("monthYear") String monthYear);

}
