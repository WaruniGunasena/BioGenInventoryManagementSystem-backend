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
public class DailySummaryReportStrategy implements ReportStrategy {

    @Autowired
    private ReportRepository reportRepo;

    @Override
    public String getReportIdentifier() { return "DAILY_SUMMARY"; }

    @Override
    public String getReportName() { return "Daily Business Summary Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        String dateStr = params.getOrDefault("date", LocalDate.now().toString());
        LocalDate date = LocalDate.parse(dateStr);

        // 1. Get raw data from DB
        Map<String, Object> rawData = reportRepo.getDailySummary(date);

        // 2. Extract values (handling nulls safely)
        BigDecimal approvedSales = (BigDecimal) rawData.getOrDefault("approvedSales", BigDecimal.ZERO);
        BigDecimal expenses = (BigDecimal) rawData.getOrDefault("dailyExpenses", BigDecimal.ZERO);

        if (approvedSales == null) approvedSales = BigDecimal.ZERO;
        if (expenses == null) expenses = BigDecimal.ZERO;

        // 3. Calculate Net Profit Estimate
        BigDecimal netProfit = approvedSales.subtract(expenses);

        // 4. Create the final clean Map for the PDF/Dashboard
        Map<String, Object> finalReport = new HashMap<>();
        finalReport.put("Date", date.toString());
        finalReport.put("Total Orders", rawData.get("totalOrders"));
        finalReport.put("Gross Sales", rawData.get("grossSales"));
        finalReport.put("Approved Sales", approvedSales);
        finalReport.put("Total Expenses (GRN)", expenses);
        finalReport.put("Estimated Net Profit", netProfit);

        // Note: For Cash/Credit breakdown, you would need a 'paymentType'
        // field in your SalesOrder model to differentiate them.

        return List.of(finalReport);
    }
}
