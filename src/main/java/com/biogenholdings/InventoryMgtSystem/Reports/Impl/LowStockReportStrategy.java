package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LowStockReportStrategy implements ReportStrategy {
    @Autowired
    private ReportRepository reportRepo;

    @Override public String getReportIdentifier() { return "LOW_STOCK"; }
    @Override public String getReportName() { return "Inventory Low Stock Alert Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        return reportRepo.getLowStockItems();
    }
}