package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.SalesOrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderPaymentRepository extends JpaRepository<SalesOrderPayment, Long> {

    List<SalesOrderPayment> findBySalesOrderId(Long salesOrderId);

    SalesOrderPayment findTopBySalesOrderIdOrderByIdDesc(Long salesOrderId);
}
