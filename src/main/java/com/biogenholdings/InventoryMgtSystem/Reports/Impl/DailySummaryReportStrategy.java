package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class DailySummaryReportStrategy implements ReportStrategy {

    @Autowired
    private ReportRepository reportRepository;
    @Override
    public String getReportIdentifier() {
        return "DAILY_SUMMARY";
    }

    @Override
    public String getReportName() {
        return "Daily Summary";
    }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        String dateStr = params.getOrDefault("date", LocalDate.now().toString());

        // 2. Parse the String into a LocalDate object
        LocalDate selectedDate = LocalDate.parse(dateStr);

        Map<String, Object> summary = reportRepository.getDailySummary(selectedDate);

        if (summary != null && !summary.isEmpty()) {
            return List.of(summary);
        }

        return List.of();
    }
}
