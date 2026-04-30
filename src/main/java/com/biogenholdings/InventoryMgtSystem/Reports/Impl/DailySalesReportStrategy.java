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
public class DailySalesReportStrategy implements ReportStrategy {
    final private ReportRepository reportRepo;

    @Override public String getReportIdentifier() { return "DAILY_SALES_REPORT"; }
    @Override public String getReportName() { return "Daily Sales Activity Report"; }


    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        // 1. Handle Dates (Support for Daily/Monthly)
        LocalDate start = LocalDate.parse(params.get("startDate"));

        Map<String, Object> summary = reportRepo.getSalesSummary(start, start);
        return List.of(summary); // Critical: wrap in List
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }

    @Override
    public List<String> getColumnOrder() {
        return List.of("Gross_sales","Net_sales","Total_Invoices");
    }
}