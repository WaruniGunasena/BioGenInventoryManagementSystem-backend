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
public class CustomerWiseReportStrategy implements ReportStrategy {
    final private ReportRepository reportRepo;
    @Override public String getReportIdentifier() { return "CUSTOMER_WISE"; }
    @Override public String getReportName() { return "Customer-wise Sales Summary Report"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        LocalDate start = LocalDate.parse(params.get("startDate"));
        LocalDate end = LocalDate.parse(params.get("endDate"));
        return reportRepo.getCustomerWiseSales(start, end);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }

    @Override
    public List<String> getColumnOrder() {
        return List.of("Customer_Name","Address", "Total_Invoices", "Total_Revenue");
    }

}