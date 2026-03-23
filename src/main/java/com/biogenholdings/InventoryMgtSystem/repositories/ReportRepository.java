package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface ReportRepository extends JpaRepository<SalesOrder, Long> {

    // 1.1 & 3.1: Daily Business/Sales Summary
    @Query(value = "SELECT " +
            "COUNT(id) as totalOrders, " +
            "SUM(grand_total) as grossSales, " +
            "SUM(CASE WHEN status = 'APPROVED' THEN grand_total ELSE 0 END) as approvedSales " +
            "FROM sales_orders " +
            "WHERE invoice_date = :date AND is_deleted = false", nativeQuery = true)
    Map<String, Object> getDailySummary(@Param("date") LocalDate date);

    // 4.5: Top Customers Report (By Revenue)
    @Query(value = "SELECT c.name as customerName, SUM(so.grand_total) as totalSpent, COUNT(so.id) as orderCount " +
            "FROM sales_orders so " +
            "JOIN customers c ON so.customer_id = c.id " +
            "WHERE so.is_deleted = false AND so.status = 'APPROVED' " +
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
            "WHERE so.status = 'APPROVED' AND so.is_deleted = false " +
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

}