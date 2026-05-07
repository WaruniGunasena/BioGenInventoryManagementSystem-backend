package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TopCustomersReportStrategy implements ReportStrategy {


    final private ReportRepository reportRepo;

    @Override
    public String getReportIdentifier() {
       return "TOP_CUSTOMER_LIST";
    }

    @Override
    public String getReportName() {
        return "Top Customer List";
    }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        return reportRepo.getTopCustomers();
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }

    @Override
    public List<String> getColumnOrder() {

        return List.of("Customer_Name","Order_Count","Total_Spent");
    }
}
