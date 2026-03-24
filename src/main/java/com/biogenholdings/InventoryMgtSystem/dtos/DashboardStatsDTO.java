package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsDTO {

    private List<Map<String, Object>> lowStock;
    private List<Map<String, Object>> expiryAlerts;
    private List<Map<String, Object>> topSelling;
    private Long pendingOrdersCount;
}
