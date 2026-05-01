package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.GRN;
import com.biogenholdings.InventoryMgtSystem.models.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface GRNRepository extends JpaRepository<GRN, Long> {

    boolean existsByInvoiceNumber(String invoiceNumber);
    List<GRN> findBySupplier(Supplier supplier);
    List<GRN> findByIsDeletedFalse();
    Page<GRN> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT g FROM GRN g JOIN FETCH g.supplier " +
            "WHERE UPPER(g.paymentStatus) IN ('PARTIAL', 'UNPAID')" +
            "AND g.isDeleted = false")
    List<GRN> findAllPendingCredits();

    @Query(value = """
        SELECT COALESCE(SUM(g.grand_total - COALESCE(gp.total_paid, 0)), 0)
        FROM grn g
        LEFT JOIN (
            SELECT grn_id, SUM(amount) AS total_paid
            FROM grn_payments
            WHERE is_deleted = 0
            GROUP BY grn_id
        ) gp ON g.id = gp.grn_id
        WHERE g.is_deleted = 0
          AND g.payment_status != 'PAID'
        """, nativeQuery = true)
    BigDecimal calculateTotalAccountsPayable();

    @Query(value = """
        SELECT COUNT(*) FROM grn
        WHERE is_deleted = 0
          AND payment_status != 'PAID'
        """, nativeQuery = true)
    long countPendingPurchases();

}