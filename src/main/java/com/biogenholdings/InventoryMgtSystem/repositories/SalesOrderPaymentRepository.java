package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.SalesOrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SalesOrderPaymentRepository extends JpaRepository<SalesOrderPayment, Long> {

    List<SalesOrderPayment> findBySalesOrderId(Long salesOrderId);

    SalesOrderPayment findTopBySalesOrderIdOrderByIdDesc(Long salesOrderId);

    @Query("SELECT p FROM SalesOrderPayment p " +
            "WHERE p.chequeDueDate BETWEEN :start AND :end " +
            "AND p.isDeleted = false")
    List<SalesOrderPayment> findUpcomingCheques(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT s FROM SalesOrderPayment s " +
            "JOIN FETCH s.salesOrder so " +
            "JOIN FETCH so.customer c " +
            "WHERE so.isDeleted = false " +
            "AND s.createdAt BETWEEN :startDate AND :endDate")
    List<SalesOrderPayment> findCompletedPayments(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    boolean existsBySalesOrderIdAndStatusIgnoreCase(Long salesOrderId, String status);
}
