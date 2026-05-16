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
public class ChequeSummaryReportStrategy implements ReportStrategy {

    private final ReportRepository reportRepository;

    @Override public String getReportIdentifier() { return "CHEQUE_SUMMARY"; }
    @Override public String getReportName() { return "Cheque Summary Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        LocalDate start = LocalDate.parse(params.get("startDate"));
        LocalDate end = LocalDate.parse(params.get("endDate"));

        // Optional status filter (e.g., if user wants a summary of only PAID cheques in a range)
        String status = params.get("status");

        return reportRepository.getChequeSummaryReport(start, end, status);
    }

    @Override
    public List<String> getColumnOrder() {
        return List.of("Date", "Customer_Name", "Cheque_No", "Cheque_Bank", "Cheque_Date", "Status_Label", "Amount");
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        return "landscape";
    }

    @Override
    public Boolean addGrandTotal() {
        return Boolean.TRUE;
    }
}