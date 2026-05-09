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
public class MonthlySummaryReportStrategy implements ReportStrategy {

    private final ReportRepository reportRepo;

    @Override
    public String getReportIdentifier() { return "MONTHLY_SUMMARY"; }

    @Override
    public String getReportName() { return "Monthly Business Summary Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        // Use the same keys as your Controller/UI
        LocalDate start = LocalDate.parse(params.get("startDate"));
        LocalDate end = LocalDate.parse(params.get("endDate"));

        // Retrieve the day-by-day list from the repository
        return reportRepo.getMonthlyBusinessSummary(start, end);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        // This report has 7 columns, landscape provides better spacing
        return "landscape";
    }

    @Override
    public List<String> getColumnOrder() {
        // These MUST match the aliases in your Native SQL exactly
        return List.of(
                "Date",
                "Gross_Sales",
                "Est_Net_Profit",
                "Supplier_Payments",
                "Cash_Collected",
                "Cheque_Collected",
                "Total_Amount"
        );
    }

    @Override
    public Boolean addGrandTotal() {
        return Boolean.TRUE;
    }
}