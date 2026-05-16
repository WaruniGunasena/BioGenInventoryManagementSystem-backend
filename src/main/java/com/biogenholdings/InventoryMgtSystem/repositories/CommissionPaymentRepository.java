package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.CommissionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CommissionPaymentRepository extends JpaRepository<CommissionPayment, Long> {

    List<CommissionPayment> findByInvoiceId(Long invoiceId);

    CommissionPayment findTopByInvoiceIdOrderByIdDesc(Long invoiceId);

    @Query("SELECT cp FROM CommissionPayment cp " +
            "JOIN FETCH cp.invoice i " +
            "JOIN FETCH i.salesRep u " +
            "WHERE cp.isDeleted = false " +
            "AND cp.createdAt BETWEEN :startDate AND :endDate")
    List<CommissionPayment> findCompletedPayments(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // CommissionPaymentRepository
    @Query("SELECT SUM(cp.amount) FROM CommissionPayment cp WHERE cp.isDeleted = false AND cp.createdAt BETWEEN :start AND :end")
    BigDecimal sumCompletedCommissionPayments(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(cp) FROM CommissionPayment cp WHERE cp.isDeleted = false AND cp.createdAt BETWEEN :start AND :end")
    long countCompletedCommissionPayments(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // MonthlyCommissionInvoiceRepository
    @Query("SELECT SUM(m.netPayout) FROM MonthlyCommissionInvoice m WHERE m.payoutStatus = 'UNPAID'")
    BigDecimal calculateTotalPendingCommissions();

    @Query("SELECT COUNT(m) FROM MonthlyCommissionInvoice m WHERE m.payoutStatus = 'UNPAID'")
    long countPendingCommissions();
}
