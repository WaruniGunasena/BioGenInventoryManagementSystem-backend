package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.DashboardStatsDTO;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import com.biogenholdings.InventoryMgtSystem.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ReportRepository reportRepo;

    @Override
    public DashboardStatsDTO getDashboardLiveStats() {
        List<Map<String, Object>> lowStock = reportRepo.getDashboardLowStock();
        List<Map<String, Object>> expiry = reportRepo.getDashboardExpiryAlerts();
        List<Map<String, Object>> topSelling = reportRepo.getDashboardTopSelling();

        // Get count from the Map returned by the count query
        Map<String, Object> countMap = reportRepo.getDashboardPendingCount();
        Long pendingCount = (Long) countMap.get("count");

        // Build and return the DTO
        return DashboardStatsDTO.builder()
                .lowStock(lowStock)
                .expiryAlerts(expiry)
                .topSelling(topSelling)
                .pendingOrdersCount(pendingCount)
                .build();
    }
}
