package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.enums.SalesOrderStatus;
import com.biogenholdings.InventoryMgtSystem.models.SalesOrder;
import com.biogenholdings.InventoryMgtSystem.projections.CustomerDueProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    Page<SalesOrder> findByIsDeleted(Boolean isDeleted, Pageable pageable);
    Page<SalesOrder> findByUser_IdAndIsDeletedFalse(Long userId, Pageable pageable);
    Long countByStatusAndIsDeletedFalse(SalesOrderStatus status);
    List<SalesOrder> findBycustomer_idAndIsDeletedFalseAndStatusNot(Long customerId, SalesOrderStatus status);

    @Query(value = """
    SELECT
        so.customer_id AS customerId,
        SUM(so.grand_total - COALESCE(p.total_paid, 0)) AS totalDue
    FROM sales_orders so
    LEFT JOIN (
        SELECT
            sales_order_id,
            SUM(amount) AS total_paid
        FROM sales_order_payments
        WHERE is_deleted = 0
        GROUP BY sales_order_id
    ) p ON so.id = p.sales_order_id
    WHERE
        so.is_deleted = 0
        AND (so.status = 'Approved' OR so.status = 'Pending')
        AND (so.payment_status IS NULL OR so.payment_status != 'PAID')
    GROUP BY so.customer_id
""", nativeQuery = true)
    List<CustomerDueProjection> getCustomerDueBalances();

    Long countByStatus(SalesOrderStatus status);

    @Query("SELECT s.invoiceDate as date, SUM(s.grandTotal) as total " +
            "FROM SalesOrder s WHERE s.invoiceDate >= :startDate " +
            "GROUP BY s.invoiceDate ORDER BY s.invoiceDate ASC")
    List<Map<String, Object>> getSalesTrend(@Param("startDate") LocalDate startDate);

    @Query("SELECT SUM(c.dueBalance) FROM Customer c WHERE c.isDeleted = false")
    BigDecimal getTotalOutstandingBalance();

    @Query("SELECT s FROM SalesOrder s JOIN FETCH s.customer " +
            "WHERE s.paymentStatus IN ('PARTIAL', 'PENDING')" +
            "AND s.isDeleted = false")
    List<SalesOrder> findAllPendingDebits();

    @Query("SELECT SUM(sp.amount) FROM SalesOrderPayment sp " +
            "WHERE sp.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumCompletedInflow(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(DISTINCT sp.salesOrder.id) FROM SalesOrderPayment sp " +
            "WHERE sp.createdAt BETWEEN :startDate AND :endDate")
    long countSalesWithPayments(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    @Query(value =
            "SELECT COALESCE(SUM(" +
                    "    /* 1. CALCULATE TRUE INVOICE TOTAL */ " +
                    "    (COALESCE(so.grand_total, 0) - " +
                    "    CASE " +
                    "        WHEN so.additional_discount_type = 'PERCENTAGE' " +
                    "        THEN (COALESCE(so.grand_total, 0) * COALESCE(so.additional_discount, 0) / 100) " +
                    "        ELSE COALESCE(so.additional_discount, 0) " +
                    "    END " +
                    "    + COALESCE(so.courier_charges, 0) " +
                    "    - COALESCE(so.return_credits, 0)) " +
                    "    /* 2. SUBTRACT PAYMENTS RECEIVED */ " +
                    "    - COALESCE(p.total_paid, 0) " +

                    "), 0) AS total_receivable " +

                    "FROM sales_orders so " +

                    "/* Subquery for Payments */ " +
                    "LEFT JOIN ( " +
                    "    SELECT sales_order_id, SUM(COALESCE(amount, 0)) AS total_paid " +
                    "    FROM sales_order_payments " +
                    "    WHERE is_deleted = false " +
                    "    GROUP BY sales_order_id " +
                    ") p ON so.id = p.sales_order_id " +

                    "WHERE so.status = 'APPROVED' " +
                    "AND so.is_deleted = false " +
                    "AND so.payment_status != 'PAID'",
            nativeQuery = true
    )
    BigDecimal calculateTotalAccountsReceivable();

    @Query(value = "SELECT COUNT(*) FROM sales_orders " +
            "WHERE status = 'Approved' AND payment_status != 'PAID' AND is_deleted = 0",
            nativeQuery = true)
    long countPendingSales();

    // 1. Approved Today's Sales
    @Query("SELECT SUM(so.grandTotal - COALESCE(so.additionalDiscount, 0) + COALESCE(so.courierCharges, 0) - COALESCE(so.returnCredits, 0)) " +
            "FROM SalesOrder so " +
            "WHERE so.status = com.biogenholdings.InventoryMgtSystem.enums.SalesOrderStatus.Approved " +
            "AND so.isDeleted = false " +
            "AND so.approvedAt BETWEEN :start AND :end")
    BigDecimal sumApprovedSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 2. Count for Dashboard widgets
    long countByStatusAndIsDeletedFalseAndApprovedAtBetween(SalesOrderStatus status, LocalDateTime start, LocalDateTime end);

}