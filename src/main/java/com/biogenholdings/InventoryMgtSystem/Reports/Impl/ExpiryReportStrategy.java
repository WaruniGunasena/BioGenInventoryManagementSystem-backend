package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExpiryReportStrategy implements ReportStrategy {
    final private ReportRepository reportRepo;
    @Override public String getReportIdentifier() { return "EXPIRY_REPORT"; }
    @Override public String getReportName() { return "Product Expiry / Near Expiry Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        // Default to 0 (already expired) if no month parameter is passed
        int months = Integer.parseInt(params.getOrDefault("months", "0"));
        return reportRepo.getExpiryReport(months);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.get(0).size();

        return columnCount > 6 ? "landscape" : "portrait";
    }
}
