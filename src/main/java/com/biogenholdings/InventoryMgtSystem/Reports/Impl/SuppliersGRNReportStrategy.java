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
public class SuppliersGRNReportStrategy implements ReportStrategy {

    final private ReportRepository reportRepository;

    @Override public String getReportIdentifier() { return "SUPPLIER_GRN"; }
    @Override public String getReportName() { return "Supplier GRN Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        LocalDate start = LocalDate.parse(params.get("start"));
        LocalDate end = LocalDate.parse(params.get("end"));

        return reportRepository.getSuppliersGRNReport(start, end);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }

    @Override
    public List<String> getColumnOrder() {
        // Must match the Aliases in the SQL Query
        return List.of("Date", "Supplier_Name", "Invoice_No", "Invoice_Amount");
    }

    @Override
    public Boolean addGrandTotal(){
        return Boolean.TRUE;
    }

}
