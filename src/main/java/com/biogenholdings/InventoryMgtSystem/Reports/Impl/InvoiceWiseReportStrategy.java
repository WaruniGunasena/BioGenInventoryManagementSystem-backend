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
public class InvoiceWiseReportStrategy implements ReportStrategy {
    final private ReportRepository reportRepo;
    @Override public String getReportIdentifier() { return "INVOICE_WISE"; }
    @Override public String getReportName() { return "Invoice-wise Sales Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        LocalDate start = LocalDate.parse(params.get("startDate"));
        LocalDate end = LocalDate.parse(params.get("endDate"));
        return reportRepo.getInvoiceWiseSales(start, end);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.get(0).size();

        return columnCount > 6 ? "landscape" : "portrait";
    }
}