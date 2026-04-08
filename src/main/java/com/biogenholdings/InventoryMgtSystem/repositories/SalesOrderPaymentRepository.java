package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.SalesOrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SalesOrderPaymentRepository extends JpaRepository<SalesOrderPayment, Long> {

    List<SalesOrderPayment> findBySalesOrderId(Long salesOrderId);

    SalesOrderPayment findTopBySalesOrderIdOrderByIdDesc(Long salesOrderId);

    @Query("SELECT p FROM SalesOrderPayment p " +
            "WHERE p.chequeDueDate BETWEEN :start AND :end " +
            "AND p.isDeleted = false")
    List<SalesOrderPayment> findUpcomingCheques(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
