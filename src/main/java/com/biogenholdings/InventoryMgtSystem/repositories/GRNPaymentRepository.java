package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.GRNPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GRNPaymentRepository extends JpaRepository<GRNPayment, Long> {

    List<GRNPayment> findByGrnId(Long grnId);

    GRNPayment findTopByGrnIdOrderByIdDesc(Long grnId);
}