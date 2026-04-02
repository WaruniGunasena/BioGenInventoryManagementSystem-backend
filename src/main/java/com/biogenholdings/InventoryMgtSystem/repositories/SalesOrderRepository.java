package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.enums.SalesOrderStatus;
import com.biogenholdings.InventoryMgtSystem.models.SalesOrder;
import com.biogenholdings.InventoryMgtSystem.projections.CustomerDueProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Page<SalesOrder> findByUser_Id(Long userId, Pageable pageable);
    Long countByStatusAndIsDeletedFalse(SalesOrderStatus status);

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

}