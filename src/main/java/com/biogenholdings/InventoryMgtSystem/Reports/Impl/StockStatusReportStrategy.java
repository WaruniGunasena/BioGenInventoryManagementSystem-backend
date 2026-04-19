package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StockStatusReportStrategy implements ReportStrategy {
    final private ReportRepository reportRepo;
    @Override public String getReportIdentifier() { return "STOCK_STATUS"; }
    @Override public String getReportName() { return "Inventory Stock Status Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        boolean outOfStockOnly = "OUT_OF_STOCK".equalsIgnoreCase(params.get("type"));
        return reportRepo.getStockStatusReport(outOfStockOnly);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }
}
