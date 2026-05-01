package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface ReportRepository extends JpaRepository<SalesOrder, Long> {

    // 1.1 & 3.1: Daily Business/Sales Summary
    @Query(value = "SELECT " +
            // 1. Order & Sales Stats
            // 1. Total Count (Stays simple)
            "(SELECT COUNT(id) FROM sales_orders WHERE invoice_date = :date AND is_deleted = false) as totalOrders, " +

// 2. Gross Sales (The Total value of all items sold today before any header adjustments)
            "(SELECT COALESCE(SUM(" +
            "    COALESCE(grand_total, 0) - " +
            "    CASE " +
            "        WHEN UPPER(additional_discount_type)= 'PERCENTAGE' " +
            "        THEN (COALESCE(grand_total, 0) * COALESCE(additional_discount, 0) / 100) " +
            "        ELSE COALESCE(additional_discount, 0) " +
            "    END " +
            "    + COALESCE(courier_charges, 0) " +
            "    - COALESCE(return_credits, 0)" +
            "), 0) FROM sales_orders WHERE invoice_date = :date AND is_deleted = false) as  grossSales, " +

// 3. Approved Sales (The TRUE money coming into the business)
            "(SELECT COALESCE(SUM(" +
            "    COALESCE(grand_total, 0) - " +
            "    CASE " +
            "        WHEN UPPER(additional_discount_type)= 'PERCENTAGE' " +
            "        THEN (COALESCE(grand_total, 0) * COALESCE(additional_discount, 0) / 100) " +
            "        ELSE COALESCE(additional_discount, 0) " +
            "    END " +
            "    + COALESCE(courier_charges, 0) " +
            "    - COALESCE(return_credits, 0)" +
            "), 0) FROM sales_orders WHERE invoice_date = :date AND status = 'Approved' AND is_deleted = false) as approvedSales, "+
            // 2. Income Breakdown (From SalesOrderPayment)
            "(SELECT COALESCE(SUM(amount), 0) FROM sales_order_payments WHERE CAST(created_at AS DATE) = :date AND UPPER(payment_method) = 'CASH' AND is_deleted = false) as cashIncome, " +
            "(SELECT COALESCE(SUM(amount), 0) FROM sales_order_payments WHERE CAST(created_at AS DATE) = :date AND UPPER(payment_method) = 'CHEQUE' AND is_deleted = false) as chequeIncome, " +

            // 3. Expenses (From GRNPayment)
            "(SELECT COALESCE(SUM(amount), 0) FROM grn_payments WHERE CAST(created_at AS DATE) = :date AND is_deleted = false) as totalExpenses " +
            "FROM (SELECT 1) as dummy", nativeQuery = true)
    Map<String, Object> getFullDailyBusinessSummary(@Param("date") LocalDate date);

    // 4.5: Top Customers Report (By Revenue)
    @Query(value = "SELECT c.name as customerName, SUM(so.grand_total) as totalSpent, COUNT(so.id) as orderCount " +
            "FROM sales_orders so " +
            "JOIN customers c ON so.customer_id = c.id " +
            "WHERE so.is_deleted = false AND UPPER(so.status) = 'APPROVED' " +
            "GROUP BY c.id ORDER BY totalSpent DESC LIMIT 10", nativeQuery = true)
    List<Map<String, Object>> getTopCustomers();

    // 5.2: Low Stock Report
    @Query(value = "SELECT p.name, p.item_code as itemCode, ps.total_quantity as currentStock, p.reorder_level as reorderLevel " +
            "FROM product_stock ps " +
            "JOIN products p ON ps.product_id = p.id " +
            "WHERE ps.total_quantity <= p.reorder_level AND p.is_deleted = false", nativeQuery = true)
    List<Map<String, Object>> getLowStockItems();

    // 10.1: Sales Rep-wise Sales
    @Query(value = "SELECT u.username as repName, SUM(so.grand_total) as totalSales " +
            "FROM sales_orders so " +
            "JOIN users u ON so.user_id = u.id " +
            "WHERE so.status = 'Approved' AND so.is_deleted = false " +
            "GROUP BY u.id", nativeQuery = true)
    List<Map<String, Object>> getSalesRepPerformance();

    // 5.1: Current Stock Report (All Items)
    @Query(value = "SELECT p.item_code as Code, p.name as Product, c.name as Category, " +
            "ps.total_quantity as Qty, ps.selling_price as Price " +
            "FROM product_stock ps " +
            "JOIN products p ON ps.product_id = p.id " +
            "JOIN categories c ON p.category_id = c.id " +
            "WHERE p.is_deleted = false", nativeQuery = true)
    List<Map<String, Object>> getCurrentStockReport();

    // 5.4 & 5.5: Expiry / Near Expiry Report
// This joins with GRNItems because that's where your 'exp_date' is stored
    @Query(value = "SELECT p.name as Product, gi.batch_number as Batch, gi.exp_date as ExpiryDate, " +
            "gi.quantity as BatchQty " +
            "FROM grn_items gi " +
            "JOIN products p ON gi.product_id = p.id " +
            "WHERE gi.exp_date <= :thresholdDate AND gi.is_deleted = false " +
            "ORDER BY gi.exp_date ASC", nativeQuery = true)
    List<Map<String, Object>> getExpiryReport(@Param("thresholdDate") LocalDate thresholdDate);

    // 4.1: Pharmacy (Customer) List Report
    @Query(value = "SELECT name as PharmacyName, email as Email, contact_no as Contact, " +
            "address as Address, credit_limit as CreditLimit " +
            "FROM customers WHERE is_deleted = false", nativeQuery = true)
    List<Map<String, Object>> getPharmacyList();

    // Handles 2.1, 2.2, 2.3, 2.4 (Order List & Status-specific lists)
    @Query(value = "SELECT so.invoice_number as Invoice, c.name as Pharmacy, " +
            "so.invoice_date as Date, so.status as Status, so.grand_total as Total " +
            "FROM sales_orders so " +
            "JOIN customers c ON so.customer_id = c.id " +
            "WHERE (:status IS NULL OR UPPER(so.status) = UPPER(:status)) " +
            "AND so.is_deleted = false " +
            "ORDER BY so.invoice_date DESC", nativeQuery = true)
    List<Map<String, Object>> getOrdersByStatus(@Param("status") String status);

    // Handles 2.5 (Detailed Item List for a specific Order)
    @Query(value = "SELECT p.name as Product, soi.quantity as Qty, " +
            "soi.selling_price as Price, soi.total_amount as SubTotal " +
            "FROM sales_order_items soi " +
            "JOIN products p ON soi.product_id = p.id " +
            "WHERE soi.sales_order_id = :orderId", nativeQuery = true)
    List<Map<String, Object>> getOrderDetails(@Param("orderId") Long orderId);

    // 1.3.1 Low Stock Alerts
    @Query(value = "SELECT p.name, ps.total_quantity as qty FROM product_stock ps " +
            "JOIN products p ON ps.product_id = p.id " +
            "WHERE ps.total_quantity <= p.reorder_level AND p.is_deleted = false", nativeQuery = true)
    List<Map<String, Object>> getDashboardLowStock();

    // 1.3.2 Expiry Alerts (Next 30 days)
    @Query(value = "SELECT p.name, gi.batch_number, gi.exp_date FROM grn_items gi " +
            "JOIN products p ON gi.product_id = p.id " +
            "WHERE gi.exp_date BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY) " +
            "AND gi.is_deleted = false", nativeQuery = true)
    List<Map<String, Object>> getDashboardExpiryAlerts();

    // 1.3.3 Pending Orders Count
    @Query(value = "SELECT COUNT(id) as count FROM sales_orders WHERE UPPER(status) = 'PENDING' AND is_deleted = false", nativeQuery = true)
    Map<String, Object> getDashboardPendingCount();

    // 1.3.4 Top Selling Items (Last 30 days)
    @Query(value = "SELECT p.name, SUM(soi.quantity) as total_sold FROM sales_order_items soi " +
            "JOIN products p ON soi.product_id = p.id " +
            "JOIN sales_orders so ON soi.sales_order_id = so.id " +
            "WHERE so.invoice_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) " +
            "AND so.status = 'Approved' " +
            "GROUP BY p.id ORDER BY total_sold DESC LIMIT 5", nativeQuery = true)
    List<Map<String, Object>> getDashboardTopSelling();

    // 3.3 Invoice-wise Report (Lists every individual Invoice)
    @Query(value = "SELECT " +
            "so.invoice_number as Invoice_Number, " +
            "so.invoice_date as Date, " +
            "c.name as Customer_Name, " +
            "so.status as Status, " +
            "/* Financial Calculation: (GrandTotal - Discount) + Courier - ReturnCredits */ " +
            "(COALESCE(so.grand_total, 0) - " +
            " CASE " +
            "   WHEN UPPER(so.additional_discount_type) = 'PERCENTAGE' " +
            "   THEN (COALESCE(so.grand_total, 0) * COALESCE(so.additional_discount, 0) / 100) " +
            "   ELSE COALESCE(so.additional_discount, 0) " +
            " END " +
            " + COALESCE(so.courier_charges, 0) " +
            " - COALESCE(so.return_credits, 0)) as Amount " +
            "FROM sales_orders so " +
            "JOIN customers c ON so.customer_id = c.id " +
            "WHERE UPPER(so.status) = 'APPROVED' " +
            "AND so.is_deleted = false " +
            "AND so.invoice_date BETWEEN :start AND :end " +
            "ORDER BY so.invoice_number ASC", nativeQuery = true)
    List<Map<String, Object>> getInvoiceWiseSales(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // 3.4 Product-wise Sales (Lists every Product sold)
    @Query(value = "SELECT p.name as Product_Name, p.item_code as Item_Code, " +
            "SUM(soi.quantity - soi.return_qty) as Total_Qty, SUM(soi.total_amount - (soi.return_qty * soi.selling_price)) as Total_Amount " +
            "FROM sales_order_items soi " +
            "JOIN products p ON soi.product_id = p.id " +
            "JOIN sales_orders so ON soi.sales_order_id = so.id " +
            "WHERE UPPER(so.status) = 'APPROVED' AND so.is_deleted = false " +
            "AND so.invoice_date BETWEEN :start AND :end " +
            "GROUP BY p.id, p.name, p.item_code " +
            "ORDER BY p.name ASC", nativeQuery = true)
    List<Map<String, Object>> getProductWiseSales(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // 3.5 Customer-wise Sales (Lists every Customer who bought)
    @Query(value = "SELECT " +
            "c.name AS customer_name, " +
            "c.address AS address, " +
            "COUNT(so.id) AS total_invoices, " +
            "COALESCE(SUM( " +
            "  COALESCE(so.grand_total, 0) - " +
            "  CASE " +
            "    WHEN UPPER(so.additional_discount_type) = 'PERCENTAGE'" +
            "    THEN (COALESCE(so.grand_total, 0) * COALESCE(so.additional_discount, 0) / 100) " +
            "    ELSE COALESCE(so.additional_discount, 0) " +
            "  END " +
            "  + COALESCE(so.courier_charges, 0) " +
            "  - COALESCE(so.return_credits, 0) " +
            "), 0) AS total_revenue " +
            "FROM sales_orders so " +
            "JOIN customers c ON so.customer_id = c.id " +
            "WHERE UPPER(so.status) = 'APPROVED' " +
            "AND so.is_deleted = false " +
            "AND so.invoice_date BETWEEN :start AND :end " +
            "GROUP BY c.id, c.name, c.address " +
            "ORDER BY total_revenue DESC",
            nativeQuery = true)
    List<Map<String, Object>> getCustomerWiseSales(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // 3.6 Sales Summary (Financial totals)
    // Note: NetSales logic usually subtracts discounts from Gross.
    @Query(value =
            "SELECT " +
                    "COUNT(CASE WHEN UPPER(status) IN ('APPROVED','PENDING') THEN 1 END) AS Total_Invoices, " +

                    "COALESCE(SUM( " +
                    "   COALESCE(grand_total, 0) - " +
                    "   CASE " +
                    "       WHEN UPPER(additional_discount_type) = 'PERCENTAGE' " +
                    "           THEN (COALESCE(grand_total, 0) * COALESCE(additional_discount, 0) / 100) " +
                    "       ELSE COALESCE(additional_discount, 0) " +
                    "   END " +
                    "   + COALESCE(courier_charges, 0) " +
                    "   - COALESCE(return_credits, 0) " +
                    "), 0) AS Gross_Sales, " +

                    "COALESCE(SUM( " +
                    "   CASE WHEN UPPER(status) = 'APPROVED' THEN " +
                    "       (COALESCE(grand_total, 0) - " +
                    "           CASE " +
                    "               WHEN UPPER(additional_discount_type) = 'PERCENTAGE' " +
                    "                   THEN (COALESCE(grand_total, 0) * COALESCE(additional_discount, 0) / 100) " +
                    "               ELSE COALESCE(additional_discount, 0) " +
                    "           END " +
                    "        + COALESCE(courier_charges, 0) " +
                    "        - COALESCE(return_credits, 0) " +
                    "       ) " +
                    "   ELSE 0 END " +
                    "), 0) AS Net_Sales " +

                    "FROM sales_orders " +
                    "WHERE is_deleted = false " +
                    "AND (status IS NULL OR UPPER(status) IN ('APPROVED', 'PENDING')) " +  // <-- FIXED SPACE
                    "AND invoice_date BETWEEN :start AND :end",
            nativeQuery = true
    )
    Map<String, Object> getSalesSummary(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // 4.2 Customer Balance Report (Overall view of what is owed)
    @Query(value = "SELECT c.name as Pharmacy, c.contact_no as Contact, " +
            "SUM(so.grand_total) as TotalInvoiced, " +
            "(SELECT COALESCE(SUM(sop.amount), 0) FROM sales_order_payments sop " +
            " JOIN sales_orders so2 ON sop.sales_order_id = so2.id " +
            " WHERE so2.customer_id = c.id AND sop.is_deleted = false) as TotalPaid, " +
            "(SUM(so.grand_total) - (SELECT COALESCE(SUM(sop.amount), 0) FROM sales_order_payments sop " +
            " JOIN sales_orders so2 ON sop.sales_order_id = so2.id " +
            " WHERE so2.customer_id = c.id AND sop.is_deleted = false)) as CreditBalance " +
            "FROM customers c " +
            "JOIN sales_orders so ON c.id = so.customer_id " +
            "WHERE c.is_deleted = false AND so.is_deleted = false " +
            "GROUP BY c.id HAVING CreditBalance > 0 " +
            "ORDER BY CreditBalance DESC", nativeQuery = true)
    List<Map<String, Object>> getCustomerBalanceReport();

    // 4.3 Outstanding Report (Specific Invoices not fully paid)
    @Query(value = "SELECT so.invoice_number as Invoice, c.name as Pharmacy, " +
            "so.invoice_date as Date, so.grand_total as BillAmount, " +
            "(SELECT COALESCE(SUM(amount), 0) FROM sales_order_payments sop " +
            " WHERE sop.sales_order_id = so.id AND sop.is_deleted = false) as PaidAmount, " +
            "(so.grand_total - (SELECT COALESCE(SUM(amount), 0) FROM sales_order_payments sop " +
            " WHERE sop.sales_order_id = so.id AND sop.is_deleted = false)) as Outstanding " +
            "FROM sales_orders so " +
            "JOIN customers c ON so.customer_id = c.id " +
            "WHERE so.is_deleted = false AND UPPER(so.status) = 'APPROVED' " +
            "HAVING Outstanding > 0 " +
            "ORDER BY so.invoice_date ASC", nativeQuery = true)
    List<Map<String, Object>> getOutstandingReport();

    // 4.4 Payment History (Log of all customer payments)
    @Query(value = "SELECT sop.created_at as Date, c.name as Pharmacy, " +
            "so.invoice_number as RefInvoice, sop.payment_method as Method, " +
            "sop.amount as PaidAmount, sop.bank as Bank " +
            "FROM sales_order_payments sop " +
            "JOIN sales_orders so ON sop.sales_order_id = so.id " +
            "JOIN customers c ON so.customer_id = c.id " +
            "WHERE sop.is_deleted = false AND sop.created_at BETWEEN :start AND :end " +
            "ORDER BY sop.created_at DESC", nativeQuery = true)
    List<Map<String, Object>> getPaymentHistory(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 5.1 & 5.3 Current / Out of Stock (Filters based on quantity)
    @Query(value = "SELECT p.item_code as Code, p.name as Product, " +
            "ps.total_quantity as AvailableQty, p.unit as Unit " +
            "FROM product_stock ps JOIN products p ON ps.product_id = p.id " +
            "WHERE p.is_deleted = false " +
            "AND (:onlyOutOfStock = false OR ps.total_quantity = 0) " +
            "ORDER BY p.name ASC", nativeQuery = true)
    List<Map<String, Object>> getStockStatusReport(@Param("onlyOutOfStock") boolean onlyOutOfStock);

    // 5.4, 5.5 Expiry Reports (Dynamic month range)
    @Query(value = "SELECT p.name as Product, gi.batch_number as Batch, " +
            "gi.exp_date as ExpiryDate, gi.quantity as BatchQty " +
            "FROM grn_items gi JOIN products p ON gi.product_id = p.id " +
            "WHERE gi.is_deleted = false AND gi.quantity > 0 " +
            "AND gi.exp_date <= DATE_ADD(CURRENT_DATE, INTERVAL :months MONTH) " +
            "ORDER BY gi.exp_date ASC", nativeQuery = true)
    List<Map<String, Object>> getExpiryReport(@Param("months") int months);

    // 5.6 Batch-wise Stock Report
    @Query(value = "SELECT p.name as Product, gi.batch_number as Batch, " +
            "gi.quantity as QtyInBatch, gi.exp_date as Expiry " +
            "FROM grn_items gi JOIN products p ON gi.product_id = p.id " +
            "WHERE gi.quantity > 0 AND p.is_deleted = false " +
            "ORDER BY p.name ASC, gi.exp_date ASC", nativeQuery = true)
    List<Map<String, Object>> getBatchWiseStock();

    // 5.7 Stock Movement (Simplified: IN from GRN, OUT from Sales)
    // Note: This is a complex union query for the movement log
    @Query(value = "(SELECT 'IN' as Type, CAST(gi.created_at AS DATE) as Date, p.name as Product, gi.quantity as Qty " +
            " FROM grn_items gi JOIN products p ON gi.product_id = p.id WHERE gi.created_at BETWEEN :start AND :end) " +
            "UNION ALL " +
            "(SELECT 'OUT' as Type, CAST(so.created_at AS DATE) as Date, p.name as Product, soi.quantity as Qty " +
            " FROM sales_order_items soi JOIN products p ON soi.product_id = p.id " +
            " JOIN sales_orders so ON soi.sales_order_id = so.id WHERE so.status = 'Approved' AND so.created_at BETWEEN :start AND :end) " +
            "ORDER BY Date DESC", nativeQuery = true)
    List<Map<String, Object>> getStockMovement(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 6.1 & 6.5 Purchase / GRN Report (List of all stock arrivals)
    @Query(value = "SELECT g.grn_number as GRN, s.name as Supplier, " +
            "g.received_date as Date, g.grand_total as BillAmount " +
            "FROM grns g JOIN suppliers s ON g.supplier_id = s.id " +
            "WHERE g.is_deleted = false AND g.received_date BETWEEN :start AND :end " +
            "ORDER BY g.received_date DESC", nativeQuery = true)
    List<Map<String, Object>> getPurchaseReport(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // 6.2 Supplier-wise Purchase (Grouping totals by supplier)
    @Query(value = "SELECT s.name as Supplier, COUNT(g.id) as TotalGRNs, " +
            "SUM(g.grand_total) as TotalPurchased " +
            "FROM grns g JOIN suppliers s ON g.supplier_id = s.id " +
            "WHERE g.is_deleted = false AND g.received_date BETWEEN :start AND :end " +
            "GROUP BY s.id ORDER BY TotalPurchased DESC", nativeQuery = true)
    List<Map<String, Object>> getSupplierWisePurchase(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // 6.3 Supplier Balance Report (What you still owe)
    @Query(value = "SELECT s.name as Supplier, s.contact_no as Contact, " +
            "SUM(g.grand_total) as TotalInvoiced, " +
            "(SELECT COALESCE(SUM(gp.amount), 0) FROM grn_payments gp " +
            " JOIN grns g2 ON gp.grn_id = g2.id " +
            " WHERE g2.supplier_id = s.id AND gp.is_deleted = false) as TotalPaid, " +
            "(SUM(g.grand_total) - (SELECT COALESCE(SUM(gp.amount), 0) FROM grn_payments gp " +
            " JOIN grns g2 ON gp.grn_id = g2.id " +
            " WHERE g2.supplier_id = s.id AND gp.is_deleted = false)) as BalanceDue " +
            "FROM suppliers s JOIN grns g ON s.id = g.supplier_id " +
            "WHERE s.is_deleted = false AND g.is_deleted = false " +
            "GROUP BY s.id HAVING BalanceDue > 0 " +
            "ORDER BY BalanceDue DESC", nativeQuery = true)
    List<Map<String, Object>> getSupplierBalanceReport();

    // 6.4 Supplier Payment Report (Log of payments made TO suppliers)
    @Query(value = "SELECT gp.created_at as Date, s.name as Supplier, " +
            "g.grn_number as RefGRN, gp.payment_method as Method, " +
            "gp.amount as PaidAmount, gp.cheque_number as ChequeNo " +
            "FROM grn_payments gp " +
            "JOIN grns g ON gp.grn_id = g.id " +
            "JOIN suppliers s ON g.supplier_id = s.id " +
            "WHERE gp.is_deleted = false AND gp.created_at BETWEEN :start AND :end " +
            "ORDER BY gp.created_at DESC", nativeQuery = true)
    List<Map<String, Object>> getSupplierPaymentLog(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 7.2 Expense Report (All payments to suppliers)
    @Query(value = "SELECT gp.created_at as Date, s.name as Supplier, gp.payment_method as Method, " +
            "gp.amount as Amount, " +
            "FROM grn_payments gp JOIN grn g ON gp.grn_id = g.id " +
            "JOIN suppliers s ON g.supplier_id = s.id " +
            "WHERE gp.is_deleted = false AND gp.created_at BETWEEN :start AND :end", nativeQuery = true)
    List<Map<String, Object>> getExpenseReport(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Opening Balance Logic: (Total Inflow - Total Outflow) BEFORE the startDate
    @Query(value = "SELECT " +
            "((SELECT COALESCE(SUM(amount), 0) FROM sales_order_payments WHERE created_at < :start AND is_deleted = false) - " +
            "(SELECT COALESCE(SUM(amount), 0) FROM grn_payments WHERE created_at < :start AND is_deleted = false)) as balance", nativeQuery = true)
    BigDecimal getOpeningBalance(@Param("start") LocalDateTime start);

    // 8.1 Sales Return Report (All approved returns)
    @Query(value = "SELECT pr.return_number as ReturnNo, so.invoice_number as RefInvoice, " +
            "c.name as Customer, pr.return_date as Date, pr.total_return_amount as Amount " +
            "FROM product_returns pr " +
            "JOIN sales_orders so ON pr.sales_order_id = so.id " +
            "JOIN customers c ON pr.customer_id = c.id " +
            "WHERE pr.status = 'Approved' AND pr.is_deleted = false " +
            "AND pr.return_date BETWEEN :start AND :end " +
            "ORDER BY pr.return_date DESC", nativeQuery = true)
    List<Map<String, Object>> getSalesReturnReport(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 8.3 & 8.4 Damaged/Expired Items Report
    // Logic: Items where is_reusable = false (Discarded items)
    @Query(value = "SELECT pr.return_date as Date, p.name as Product, " +
            "pri.quantity as Qty, pri.return_reason as Reason, pri.sub_total as LossAmount " +
            "FROM product_return_items pri " +
            "JOIN product_returns pr ON pri.product_return_id = pr.id " +
            "JOIN products p ON pri.product_id = p.id " +
            "WHERE pr.status = 'Approved' AND pri.is_reusable = false " +
            "AND pr.return_date BETWEEN :start AND :end " +
            "ORDER BY pr.return_date DESC", nativeQuery = true)
    List<Map<String, Object>> getNonReusableReturnReport(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Fetch Customer Header Details
    @Query(value = "SELECT c.name, c.address, c.credit_period, c.due_balance, MAX(pay.created_at) AS Last_Tx_Date, "+
    "(SELECT pay2.amount FROM sales_order_payments pay2 INNER JOIN sales_orders s2 ON pay2.sales_order_id = s2.id " +
    " WHERE s2.customer_id = c.id AND pay2.is_deleted = false ORDER BY pay2.created_at DESC, pay2.id DESC  LIMIT 1 ) AS Last_Tx_Amount "+
    "FROM customers c LEFT JOIN sales_orders s ON s.customer_id = c.id LEFT JOIN sales_order_payments pay ON pay.sales_order_id = s.id AND pay.is_deleted = false " +
    "WHERE c.id = :cId GROUP BY c.id, c.name, c.address, c.credit_period, c.due_balance;", nativeQuery = true)
    Map<String, Object> getCustomerDetailHeader(@Param("cId") Long customerId);

//    @Query(value =
//            "SELECT " +
//                    "   s.invoice_number AS Invoice_Number, " +
//                    "   s.invoice_date AS Invoice_Date, " +
//                    "   /* Standardized Formula: (GrandTotal - Discount) + Courier - Returns */ " +
//                    "   (COALESCE(s.grand_total, 0) - " +
//                    "    CASE " +
//                    "       WHEN UPPER(so.additional_discount_type) = 'PERCENTAGE'" +
//                    "       THEN (COALESCE(s.grand_total, 0) * COALESCE(s.additional_discount, 0) / 100) " +
//                    "       ELSE COALESCE(s.additional_discount, 0) " +
//                    "    END " +
//                    "    + COALESCE(s.courier_charges, 0) " +
//                    "    - COALESCE(s.return_credits, 0)) AS Amount, " +
//                    "   s.status AS Status, " +
//                    "   s.payment_status AS Payment_Status, " +
//                    "   CASE " +
//                    "       WHEN UPPER(s.payment_status) = 'PAID' THEN 'SETTLED' " +
//                    "       WHEN UPPER(TRIM(c.credit_period)) = 'CASH' THEN 'CASH SALE' " +
//                    "       /* Check if credit_period is a valid number */ " +
//                    "       WHEN c.credit_period REGEXP '^[0-9]+$' THEN " +
//                    "           CASE " +
//                    "               WHEN DATEDIFF(CURDATE(), s.invoice_date) > CAST(c.credit_period AS SIGNED) " +
//                    "               THEN 'OVERDUE' " +
//                    "               ELSE CONCAT(CAST(c.credit_period AS SIGNED) - DATEDIFF(CURDATE(), s.invoice_date), ' DAYS LEFT') " +
//                    "           END " +
//                    "       ELSE 'N/A' " +
//                    "   END AS Aging_Status " +
//                    "FROM sales_orders s " +
//                    "INNER JOIN customers c ON s.customer_id = c.id " +
//                    "WHERE s.customer_id = :cId " +
//                    "AND s.is_deleted = false " +
//                    "ORDER BY s.invoice_date DESC",
//            nativeQuery = true
//    )
//    List<Map<String, Object>> getCustomerInvoiceHistory(@Param("cId") Long customerId);
@Query(value =
        "SELECT " +
                "   s.invoice_date AS Invoice_Date, " +
                "   s.invoice_number AS Invoice_Number, " +
                "   /* 1. NET INVOICE AMOUNT */ " +
                "   (COALESCE(s.grand_total, 0) - " +
                "    CASE " +
                "       WHEN UPPER(s.additional_discount_type) = 'PERCENTAGE'" +
                "       THEN (COALESCE(s.grand_total, 0) * COALESCE(s.additional_discount, 0) / 100) " +
                "       ELSE COALESCE(s.additional_discount, 0) " +
                "    END " +
                "    + COALESCE(s.courier_charges, 0) " +
                "    - COALESCE(s.return_credits, 0)) AS Amount, " +
                "   /* 2. PAID AMOUNT (From Payments Table) */ " +
                "   COALESCE(p.total_paid, 0) AS Paid_Amount, " +
                "   /* 3. CALCULATE BALANCE (Amount - Paid_Amount) */ " +
                "   ((COALESCE(s.grand_total, 0) - " +
                "    CASE " +
                "       WHEN UPPER(s.additional_discount_type) = 'PERCENTAGE' " +
                "       THEN (COALESCE(s.grand_total, 0) * COALESCE(s.additional_discount, 0) / 100) " +
                "       ELSE COALESCE(s.additional_discount, 0) " +
                "    END " +
                "    + COALESCE(s.courier_charges, 0) " +
                "    - COALESCE(s.return_credits, 0)) - COALESCE(p.total_paid, 0)) AS Balance, " +
                "   /* 4. AGE IN DAYS */ " +
                "   CONCAT(DATEDIFF(CURDATE(), s.invoice_date), ' DAYS') AS Age_Days " +
                "FROM sales_orders s " +
                "INNER JOIN customers c ON s.customer_id = c.id " +
                "/* This subquery sums up all payments for each invoice */ " +
                "LEFT JOIN ( " +
                "   SELECT sales_order_id, SUM(amount) AS total_paid " +
                "   FROM sales_order_payments " +
                "   WHERE is_deleted = false " +
                "   GROUP BY sales_order_id " +
                ") p ON s.id = p.sales_order_id " +
                "WHERE s.customer_id = :cId " +
                "AND s.is_deleted = false " +
                "ORDER BY s.invoice_date DESC",
        nativeQuery = true
)
List<Map<String, Object>> getCustomerInvoiceHistory(@Param("cId") Long customerId);

    @Query(value =
            "SELECT " +
                    "   c.name AS Customer_Name, " +
                    "   s.invoice_number AS Invoice_Number, " +
                    "   s.status AS Status, " +
                    "   /* 1. Net Invoice Amount Calculation */ " +
                    "   (COALESCE(s.grand_total, 0) - " +
                    "    CASE " +
                    "       WHEN UPPER(s.additional_discount_type) = 'PERCENTAGE'" +
                    "       THEN (COALESCE(s.grand_total, 0) * COALESCE(s.additional_discount, 0) / 100) " +
                    "       ELSE COALESCE(s.additional_discount, 0) " +
                    "    END " +
                    "    + COALESCE(s.courier_charges, 0) " +
                    "    - COALESCE(s.return_credits, 0)) AS Amount, " +
                    "   /* 2. Paid Amount for this specific invoice */ " +
                    "   COALESCE(p.total_paid, 0) AS Paid_Amount, " +
                    "   /* 3. Balance for this specific invoice */ " +
                    "   ((COALESCE(s.grand_total, 0) - " +
                    "    CASE " +
                    "       WHEN UPPER(s.additional_discount_type) = 'PERCENTAGE'" +
                    "       THEN (COALESCE(s.grand_total, 0) * COALESCE(s.additional_discount, 0) / 100) " +
                    "       ELSE COALESCE(s.additional_discount, 0) " +
                    "    END " +
                    "    + COALESCE(s.courier_charges, 0) " +
                    "    - COALESCE(s.return_credits, 0)) - COALESCE(p.total_paid, 0)) AS Balance, " +
                    "   CONCAT(DATEDIFF(CURDATE(), s.invoice_date), ' DAYS') AS Age_Days " +
                    "FROM sales_orders s " +
                    "INNER JOIN customers c ON s.customer_id = c.id " +
                    "LEFT JOIN ( " +
                    "   SELECT sales_order_id, SUM(amount) AS total_paid " +
                    "   FROM sales_order_payments " +
                    "   WHERE is_deleted = false " +
                    "   GROUP BY sales_order_id " +
                    ") p ON s.id = p.sales_order_id " +
                    "WHERE s.is_deleted = false " +
                    "ORDER BY s.invoice_number ASC",
            nativeQuery = true
    )
    List<Map<String, Object>> getAllIndividualSalesByDate();
}