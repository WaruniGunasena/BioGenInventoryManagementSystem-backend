package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BatchStockReportStrategy implements ReportStrategy {
    final private ReportRepository reportRepo;
    @Override public String getReportIdentifier() { return "BATCH_STOCK"; }
    @Override public String getReportName() { return "Batch-wise Inventory Breakdown"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        return reportRepo.getBatchWiseStock();
    }
}
