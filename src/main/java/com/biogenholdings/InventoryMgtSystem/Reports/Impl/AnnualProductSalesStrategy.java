package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AnnualProductSalesStrategy implements ReportStrategy {
    private final ReportRepository reportRepo;

    @Override public String getReportIdentifier() { return "ANNUAL_PRODUCT_SALES"; }
    @Override public String getReportName() { return "Product-wise Sales Annual"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        // Default to current year if not provided
        int year = params.containsKey("year") ? Integer.parseInt(params.get("year")) : 2026;
        return reportRepo.getAnnualProductSales(year);
    }

    @Override
    public List<String> getColumnOrder() {
        return List.of(
                "Product_Name", "JANUARY", "FEBRUARY", "MARCH", "APRIL",
                "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER",
                "OCTOBER", "NOVEMBER", "DECEMBER"
        );
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        // 13 columns always require landscape to be readable
        return "landscape";
    }
}
