package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SupplierCreditDetailsReportStrategy implements ReportStrategy {

    private final ReportRepository reportRepository;

    @Override
    public String getReportIdentifier() {
        return "SUPPLIER_CREDIT_SUMMARY";
    }

    @Override
    public String getReportName() {
        return "Supplier-wise Credit Details Summary Report";
    }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        // This report filters by supplierId rather than a date range
        Long supplierId = Long.parseLong(params.get("supplierId"));
        return reportRepository.getSupplierCreditDetails(supplierId);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        // With 6 columns, portrait usually fits, but we follow your logic
        int columnCount = data.getFirst().size();
        return columnCount > 6 ? "landscape" : "portrait";
    }

    @Override
    public List<String> getColumnOrder() {
        // These match the Aliases in the native SQL query
        return List.of("Date", "Invoice_No", "Amount", "Paid_Amount", "Balance", "Age_Days");
    }

    @Override
    public Boolean addGrandTotal(){
        return Boolean.TRUE;
    }
}