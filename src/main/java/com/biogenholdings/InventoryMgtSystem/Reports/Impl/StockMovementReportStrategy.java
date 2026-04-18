package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StockMovementReportStrategy implements ReportStrategy {
    final private ReportRepository reportRepo;
    @Override public String getReportIdentifier() { return "STOCK_MOVEMENT"; }
    @Override public String getReportName() { return "Stock In/Out Movement Log"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        LocalDate start = LocalDate.parse(params.get("startDate"));
        LocalDate end = LocalDate.parse(params.get("endDate"));
        return reportRepo.getStockMovement(start.atStartOfDay(), end.atTime(23, 59, 59));
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }
}