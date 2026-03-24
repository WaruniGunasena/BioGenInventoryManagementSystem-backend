package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.GRN;
import com.biogenholdings.InventoryMgtSystem.models.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GRNRepository extends JpaRepository<GRN, Long> {

    boolean existsByInvoiceNumber(String invoiceNumber);
    List<GRN> findBySupplier(Supplier supplier);
    List<GRN> findByIsDeletedFalse();
    Page<GRN> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT g FROM GRN g JOIN FETCH g.supplier " +
            "WHERE g.paymentStatus IN ('PARTIAL', 'UNPAID')")
    List<GRN> findAllPendingCredits();

}