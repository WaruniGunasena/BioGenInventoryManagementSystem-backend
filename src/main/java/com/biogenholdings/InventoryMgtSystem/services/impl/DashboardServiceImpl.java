package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.DashboardResponseDTO;
import com.biogenholdings.InventoryMgtSystem.enums.SalesOrderStatus;
import com.biogenholdings.InventoryMgtSystem.repositories.GRNItemRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.ProductRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.SalesOrderItemRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.SalesOrderRepository;
import com.biogenholdings.InventoryMgtSystem.services.DashboardService;
import com.biogenholdings.InventoryMgtSystem.services.StockStatusProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final GRNItemRepository grnItemRepository;

    public DashboardResponseDTO getDashboardData() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        LocalDate expiryThreshold = today.plusMonths(3);

        // 1. Fetch Summary Data
        long pendingOrders = salesOrderRepository.countByStatus(SalesOrderStatus.Pending);
        BigDecimal outstanding = salesOrderRepository.getTotalOutstandingBalance();
        if (outstanding == null) outstanding = BigDecimal.ZERO;

        // 2. Fetch Stock & Expiry Data
        List<Map<String, Object>> lowStockItems = productRepository.findLowStockProducts();
        List<Map<String, Object>> expiringItems = grnItemRepository.findUpcomingExpiries(expiryThreshold);

        // 3. Fetch Top Selling (Limit to Top 5 for the Graph)
        List<Map<String, Object>> topSelling = salesOrderItemRepository.findTopSellingProducts(PageRequest.of(0, 5));

        // 4. Fetch Sales Trend (Last 30 Days)
        List<Map<String, Object>> trend = salesOrderRepository.getSalesTrend(thirtyDaysAgo);

        // 5. Get Stock Pie Chart Data
        StockStatusProjection stockCounts = productRepository.getStockStatusCounts();
        Map<String, Long> stockDist = new HashMap<>();

        if (stockCounts != null) {
            stockDist.put("Out of Stock", stockCounts.getOutOfStock() != null ? stockCounts.getOutOfStock() : 0L);
            stockDist.put("Low Stock", stockCounts.getLowStock() != null ? stockCounts.getLowStock() : 0L);
            stockDist.put("Healthy", stockCounts.getHealthy() != null ? stockCounts.getHealthy() : 0L);
        }

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // Today's Approved Total
        BigDecimal todayTotal = salesOrderRepository.sumApprovedSalesBetween(todayStart, now);

        // Monthly Approved Total
        BigDecimal monthlyTotal = salesOrderRepository.sumApprovedSalesBetween(monthStart, now);


       BigDecimal todaySales = todayTotal != null ? todayTotal : BigDecimal.ZERO;
       BigDecimal monthlySales = monthlyTotal != null ? monthlyTotal : BigDecimal.ZERO;



        return DashboardResponseDTO.builder()
                .pendingOrdersCount(pendingOrders)
                .totalOutstandingBalance(outstanding)
                .todaySales(todaySales)
                .monthlySales(monthlySales)
                .lowStockCount((long) lowStockItems.size())
                .upcomingExpiriesCount((long) expiringItems.size())
                .lowStockList(lowStockItems)
                .expiryList(expiringItems)
                .topSellingProducts(topSelling)
                .salesTrend(trend)
                .stockStatusDistribution(stockDist)
                .build();
    }
}
