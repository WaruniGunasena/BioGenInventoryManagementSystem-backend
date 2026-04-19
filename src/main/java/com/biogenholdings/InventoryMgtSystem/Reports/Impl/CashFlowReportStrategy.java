package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.services.CashFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CashFlowReportStrategy implements ReportStrategy {
    final private CashFlowService cashFlowService;

    @Override public String getReportIdentifier() { return "CASH_FLOW_DETAILED"; }
    @Override public String getReportName() { return "Detailed Cash Flow Report (Credits & Debits)"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        LocalDate start = LocalDate.parse(params.get("startDate"));
        LocalDate end = LocalDate.parse(params.get("endDate"));

        var flow = cashFlowService.getCompletedCashFlow(start, end);
        List<Map<String, Object>> reportRows = new ArrayList<>();

        // Add Credits (Outflow)
        flow.getCredits().forEach(c -> {
            Map<String, Object> r = new HashMap<>();
            r.put("Date", c.getDate());
            r.put("Type", "CREDIT (Out)");
            r.put("Entity", c.getSupplier());
            r.put("Ref", c.getInvoiceNumber());
            r.put("Amount", c.getAmount().negate()); // Show as negative
            reportRows.add(r);
        });

        // Add Debits (Inflow)
        flow.getDebits().forEach(d -> {
            Map<String, Object> r = new HashMap<>();
            r.put("Date", d.getDate());
            r.put("Type", "DEBIT (In)");
            r.put("Entity", d.getCustomer());
            r.put("Ref", d.getInvoiceNumber());
            r.put("Amount", d.getAmount());
            reportRows.add(r);
        });

        return reportRows;
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }
}