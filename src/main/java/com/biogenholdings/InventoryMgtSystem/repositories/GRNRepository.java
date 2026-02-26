package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.GRN;
import com.biogenholdings.InventoryMgtSystem.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GRNRepository extends JpaRepository<GRN, Long> {

    boolean existsByInvoiceNumber(String invoiceNumber);
    List<GRN> findBySupplier(Supplier supplier);
}