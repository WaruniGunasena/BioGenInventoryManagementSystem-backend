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
public class SalesReportStrategy implements ReportStrategy {
    final private ReportRepository reportRepo;

    @Override public String getReportIdentifier() { return "SALES_REPORT"; }
    @Override public String getReportName() { return "Sales Activity Report"; }


    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        // 1. Handle Dates (Support for Daily/Monthly)
        LocalDate start = LocalDate.parse(params.get("startDate"));
        LocalDate end = LocalDate.parse(params.get("endDate"));

        Map<String, Object> summary = reportRepo.getSalesSummary(start, end);
        return List.of(summary); // Critical: wrap in List
    }
}