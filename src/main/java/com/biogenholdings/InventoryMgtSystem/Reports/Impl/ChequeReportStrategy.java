package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChequeReportStrategy implements ReportStrategy {

    private final ReportRepository reportRepository;

    @Override public String getReportIdentifier() { return "PENDING_CHEQUE_REPORT"; }

    @Override
    public String getReportName() {
        return "Pending Cheque report Summary";
    }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        String status = params.getOrDefault("status", "REALIZING");
        return reportRepository.getChequeReportByStatus(status);
    }

    @Override
    public List<String> getColumnOrder() {
        // Matches the screenshot requirements
        return List.of("Date", "Customer_Name", "Cheque_No", "Cheque_Bank", "Cheque_Date", "Status_Or_Age", "Amount");
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        return "landscape"; // Better for 7 columns
    }

    @Override
    public Boolean addGrandTotal() {
        return Boolean.TRUE;
    }
}