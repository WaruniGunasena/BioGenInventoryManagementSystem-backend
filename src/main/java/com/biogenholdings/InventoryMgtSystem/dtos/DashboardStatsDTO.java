package com.biogenholdings.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsDTO {

    private List<Map<String, Object>> lowStock;
    private List<Map<String, Object>> expiryAlerts;
    private List<Map<String, Object>> topSelling;
    private Long pendingOrdersCount;
}
