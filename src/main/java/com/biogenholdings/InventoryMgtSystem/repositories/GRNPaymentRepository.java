package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.GRNPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GRNPaymentRepository extends JpaRepository<GRNPayment, Long> {

    List<GRNPayment> findByGrnId(Long grnId);

    GRNPayment findTopByGrnIdOrderByIdDesc(Long grnId);

    @Query("SELECT p FROM GRNPayment p " +
            "JOIN FETCH p.grn g " +
            "JOIN FETCH g.supplier s " +
            "WHERE g.isDeleted = false " +
            "AND p.createdAt BETWEEN :startDate AND :endDate")
    List<GRNPayment> findCompletedPayments(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}