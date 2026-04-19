package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.ProductReturn;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ProductReturnRepository extends JpaRepository<ProductReturn, Long> {

    // Find a specific return by its unique audit number
    Optional<ProductReturn> findByReturnNumber(String returnNumber);

    Page<ProductReturn> findByIsDeletedFalse(@Nonnull Pageable pageable);

    // Custom Query: Calculate total commission reversed for a Sales Rep in a date range
    @Query("SELECT SUM(pr.totalCommissionReversal) FROM ProductReturn pr " +
            "WHERE pr.salesRep.id = :repId AND pr.returnDate BETWEEN :startDate AND :endDate")
    Double getTotalCommissionReversal(@Param("repId") Long repId,
                                      @Param("startDate") java.time.LocalDateTime startDate,
                                      @Param("endDate") java.time.LocalDateTime endDate);


}
