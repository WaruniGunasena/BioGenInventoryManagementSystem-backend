package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.services.CashFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProfitAndLossReportStrategy implements ReportStrategy {
    final private CashFlowService cashFlowService;

    @Override public String getReportIdentifier() { return "PROFIT_LOSS"; }
    @Override public String getReportName() { return "Profit and Loss Statement"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        LocalDate start = LocalDate.parse(params.get("startDate"));
        LocalDate end = LocalDate.parse(params.get("endDate"));

        var summaryResponse = cashFlowService.getCashFlowSummary(start, end);
        var summary = summaryResponse.getCashFlowSummary();

        Map<String, Object> row = new HashMap<>();
        row.put("Period", start + " to " + end);
        row.put("Total Revenue (Inflow)", summary.getNetCashInflow());
        row.put("Total Expenses (Outflow)", summary.getNetCashOutflow());
        row.put("Net Operating Profit", summary.getOperatingCashFlow());
        row.put("Accounts Receivable", summary.getAccountsReceivable());
        row.put("Accounts Payable", summary.getAccountsPayable());

        return List.of(row);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }
}
