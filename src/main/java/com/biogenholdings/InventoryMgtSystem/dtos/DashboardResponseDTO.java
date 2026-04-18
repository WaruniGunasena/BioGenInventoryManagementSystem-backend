package com.biogenholdings.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponseDTO {
    // Summary Cards
    private long pendingOrdersCount;
    private BigDecimal totalOutstandingBalance;
    private long lowStockCount;
    private long upcomingExpiriesCount;

    // Charts Data
    private List<Map<String, Object>> topSellingProducts; // Bar Chart
    private List<Map<String, Object>> salesTrend;         // Line Chart
    private Map<String, Long> stockStatusDistribution;    // Pie Chart

    // Lists for Tables/Alerts
    private List<Map<String, Object>> lowStockList;
    private List<Map<String, Object>> expiryList;
}
