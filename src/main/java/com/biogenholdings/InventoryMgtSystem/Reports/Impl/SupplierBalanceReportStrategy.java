package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SupplierBalanceReportStrategy implements ReportStrategy {
    final private ReportRepository reportRepo;
    @Override public String getReportIdentifier() { return "SUPPLIER_BALANCE"; }
    @Override public String getReportName() { return "Supplier Outstanding Balance Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        return reportRepo.getSupplierBalanceReport();
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.get(0).size();

        return columnCount > 6 ? "landscape" : "portrait";
    }
}