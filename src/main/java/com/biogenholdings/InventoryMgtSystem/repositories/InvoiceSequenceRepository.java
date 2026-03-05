package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.InvoiceSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Long> {
}
