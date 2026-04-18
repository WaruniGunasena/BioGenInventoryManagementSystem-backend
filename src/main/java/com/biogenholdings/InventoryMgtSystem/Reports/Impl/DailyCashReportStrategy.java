package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import com.biogenholdings.InventoryMgtSystem.services.CashFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DailyCashReportStrategy implements ReportStrategy {
    final private ReportRepository reportRepo;
    final private CashFlowService cashFlowService;

    @Override public String getReportIdentifier() { return "DAILY_CASH_BALANCE"; }
    @Override public String getReportName() { return "Opening and Closing Cash Balance Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        LocalDate date = LocalDate.parse(params.getOrDefault("date", LocalDate.now().toString()));
        LocalDateTime start = date.atStartOfDay();

        BigDecimal opening = reportRepo.getOpeningBalance(start);
        var summary = cashFlowService.getCashFlowSummary(date, date).getCashFlowSummary();
        BigDecimal closing = opening.add(summary.getOperatingCashFlow());

        Map<String, Object> row = new HashMap<>();
        row.put("Date", date);
        row.put("Opening Balance", opening);
        row.put("Today's Inflow", summary.getNetCashInflow());
        row.put("Today's Outflow", summary.getNetCashOutflow());
        row.put("Closing Balance", closing);

        return List.of(row);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }
}
