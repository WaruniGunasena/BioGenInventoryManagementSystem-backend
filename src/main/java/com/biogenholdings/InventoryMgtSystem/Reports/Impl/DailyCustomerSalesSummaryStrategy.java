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
public class DailyCustomerSalesSummaryStrategy implements ReportStrategy {
    private final ReportRepository reportRepo;

    @Override public String getReportIdentifier() { return "DAILY_CUSTOMER_SUMMARY"; }
    @Override public String getReportName() { return "Customer-wise Sales Credit Details Summary Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        LocalDate date = LocalDate.parse(params.get("date"));
        return reportRepo.getAllIndividualSalesByDate(date);
    }

    @Override
    public List<String> getColumnOrder() {
        return List.of("Customer_Name", "Invoice_Number", "Amount", "Paid_Amount", "Balance","Age_Days");
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        return "portrait";
    }
}
