package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.ProductReturn;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface ProductReturnRepository extends JpaRepository<ProductReturn, Long> {

    Optional<ProductReturn> findByReturnNumber(String returnNumber);

    Page<ProductReturn> findByIsDeletedFalse(@Nonnull Pageable pageable);

    @Query("SELECT SUM(pr.totalCommissionReversal) FROM ProductReturn pr " +
            "WHERE pr.salesRep.id = :repId " +
            "AND pr.status = 'Approved' " +
            "AND FUNCTION('DATE_FORMAT', pr.returnDate, '%Y-%m') = :monthYear")
    BigDecimal getTotalReversalForRep(@Param("repId") Long repId, @Param("monthYear") String monthYear);

    @Query("SELECT pr FROM ProductReturn pr " +
            "LEFT JOIN FETCH pr.salesOrder " +
            "LEFT JOIN FETCH pr.customer " +
            "LEFT JOIN FETCH pr.salesRep " +
            "WHERE pr.salesRep.id = :salesRepId " +
            "AND pr.returnDate BETWEEN :startDate AND :endDate " +
            "ORDER BY pr.returnDate DESC")
    Page<ProductReturn> findReversalsBySalesRepAndDateRange(
            @Param("salesRepId") Long salesRepId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT pr FROM ProductReturn pr " +
            "LEFT JOIN FETCH pr.salesOrder " +
            "LEFT JOIN FETCH pr.customer " +
            "WHERE pr.salesRep.id = :repId " +
            "AND FUNCTION('DATE_FORMAT', pr.returnDate, '%Y-%m') = :monthYear " +
            "ORDER BY pr.returnDate DESC")
    List<ProductReturn> findByRepAndMonth(
            @Param("repId") Long repId,
            @Param("monthYear") String monthYear
    );
}
