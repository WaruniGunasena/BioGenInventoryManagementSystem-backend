package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.ProductReturn;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ProductReturnRepository extends JpaRepository<ProductReturn, Long> {

    // Find a specific return by its unique audit number
    Optional<ProductReturn> findByReturnNumber(String returnNumber);

    Page<ProductReturn> findByIsDeletedFalse(@Nonnull Pageable pageable);

    // Get all returns linked to a specific Sales Order/Invoice
    List<ProductReturn> findBySalesOrderId(Long salesOrderId);

    // Get all returns for a specific Customer
    List<ProductReturn> findByCustomerId(Long customerId);

    // Get all returns handled by a specific Sales Rep (User)
    List<ProductReturn> findBySalesRepId(Long salesRepId);

    // Custom Query: Calculate total commission reversed for a Sales Rep in a date range
    @Query("SELECT SUM(pr.totalCommissionReversal) FROM ProductReturn pr " +
            "WHERE pr.salesRep.id = :repId AND pr.returnDate BETWEEN :startDate AND :endDate")
    Double getTotalCommissionReversal(@Param("repId") Long repId,
                                      @Param("startDate") java.time.LocalDateTime startDate,
                                      @Param("endDate") java.time.LocalDateTime endDate);


}
