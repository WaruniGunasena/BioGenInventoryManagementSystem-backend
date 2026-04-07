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
public class PurchaseReportStrategy implements ReportStrategy {
    final private ReportRepository reportRepo;
    @Override public String getReportIdentifier() { return "PURCHASE_REPORT"; }
    @Override public String getReportName() { return "Goods Received & Purchase Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        LocalDate start = LocalDate.parse(params.getOrDefault("startDate", LocalDate.now().toString()));
        LocalDate end = LocalDate.parse(params.getOrDefault("endDate", LocalDate.now().toString()));
        String type = params.getOrDefault("type", "LIST");

        if ("SUPPLIER_WISE".equalsIgnoreCase(type)) {
            return reportRepo.getSupplierWisePurchase(start, end);
        }
        return reportRepo.getPurchaseReport(start, end);
    }
}