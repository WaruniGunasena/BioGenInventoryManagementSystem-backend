package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.CommissionPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommissionPaymentRepository extends JpaRepository<CommissionPayment, Long> {

    List<CommissionPayment> findByInvoiceId(Long invoiceId);

    CommissionPayment findTopByInvoiceIdOrderByIdDesc(Long invoiceId);
}
