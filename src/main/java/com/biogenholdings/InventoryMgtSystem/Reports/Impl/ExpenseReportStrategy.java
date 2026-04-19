package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ExpenseReportStrategy implements ReportStrategy {

    @Autowired
    private ReportRepository reportRepo;

    @Override
    public String getReportIdentifier() {
        return "EXPENSE_REPORT";
    }

    @Override
    public String getReportName() {
        return "Detailed Expense Report (Supplier Payments)";
    }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        // Parse dates from parameters
        LocalDate startDate = LocalDate.parse(params.getOrDefault("startDate", LocalDate.now().toString()));
        LocalDate endDate = LocalDate.parse(params.getOrDefault("endDate", LocalDate.now().toString()));

        // Convert to LocalDateTime to cover the full range of the start and end days
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return reportRepo.getExpenseReport(start, end);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }
}
