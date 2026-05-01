package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.MonthlyCommissionInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MonthlyCommissionInvoiceRepository extends JpaRepository<MonthlyCommissionInvoice, Long> {

    @Query("SELECT m FROM MonthlyCommissionInvoice m " +
            "JOIN FETCH m.salesRep")
    Page<MonthlyCommissionInvoice> findAllInvoicesPaginated(Pageable pageable);

    @Query("SELECT m FROM MonthlyCommissionInvoice m " +
            "JOIN FETCH m.salesRep " +
            "WHERE m.commissionInvoiceNumber = :invoiceNumber")
    Optional<MonthlyCommissionInvoice> findByCommissionInvoiceNumber(@Param("invoiceNumber") String invoiceNumber);

    @Query("SELECT m FROM MonthlyCommissionInvoice m " +
            "WHERE m.salesRep.id = :userId " +
            "ORDER BY m.monthYear DESC")
    Page<MonthlyCommissionInvoice> findBySalesRepId(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
