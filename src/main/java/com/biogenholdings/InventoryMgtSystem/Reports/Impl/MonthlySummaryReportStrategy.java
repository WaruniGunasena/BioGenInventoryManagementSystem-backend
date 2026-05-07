package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MonthlySummaryReportStrategy implements ReportStrategy {

    @Autowired
    private ReportRepository reportRepo;

    @Override
    public String getReportIdentifier() { return "MONTHLY_SUMMARY"; }

    @Override
    public String getReportName() { return "Monthly Business Summary Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        String dateStr = params.getOrDefault("date", LocalDate.now().toString());
        LocalDate date = LocalDate.parse(dateStr);

        Map<String, Object> data = reportRepo.getFullDailyBusinessSummary(date);

        // Convert everything to BigDecimal safely
        BigDecimal approvedSales = new BigDecimal(data.get("approvedSales").toString());
        BigDecimal expenses = new BigDecimal(data.get("totalExpenses").toString());
        BigDecimal cashIn = new BigDecimal(data.get("cashIncome").toString());
        BigDecimal chequeIn = new BigDecimal(data.get("chequeIncome").toString());

        // Calculations
        BigDecimal totalCollection = cashIn.add(chequeIn);
        BigDecimal estimatedNetProfit = approvedSales.subtract(expenses);

        Map<String, Object> summary = new HashMap<>();
        summary.put("Date", date);
        summary.put("Orders Count", data.get("totalOrders"));
        summary.put("Gross Sales", data.get("grossSales"));
        summary.put("Approved Sales", approvedSales);
        summary.put("Cash Collected", cashIn);
        summary.put("Cheque Collected", chequeIn);
        summary.put("Total Collection", totalCollection);
        summary.put("Supplier Payments (Expenses)", expenses);
        summary.put("Est. Net Profit", estimatedNetProfit);

        return List.of(summary);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }

    @Override
    public List<String> getColumnOrder() {

        return List.of("Date","Gross_sales","Est_Net_sales","Supplier_Payments","Cash_Collected","Cheque_Collected","Total_Collection");
    }
}
