package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IndividualCustomerReportStrategy implements ReportStrategy {
    private final ReportRepository reportRepo;

    @Override public String getReportIdentifier() { return "INDIVIDUAL_CUSTOMER"; }
    @Override public String getReportName() { return "Customer Sales Credit Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        Long customerId = Long.parseLong(params.get("customerId"));

        // 1. Get Header Info
        Map<String, Object> header = reportRepo.getCustomerDetailHeader(customerId);

        // 2. Get Invoice List

        // Logic: To show header info in the same PDF using your current Table Utility,
        // we can prepend the header info as rows or just return the invoice list
        // and rely on the "GRAND TOTAL" logic we added to the utility for the 'Due Balance'.

        return reportRepo.getCustomerInvoiceHistory(customerId);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }

    @Override
    public Boolean addGrandTotal(){
        return Boolean.TRUE;
    }

    @Override
    public List<String> getColumnOrder() {
        return List.of("Invoice_Date","Invoice_Number", "Amount","Paid_Amount", "Balance","Age_Days");
    }
}
