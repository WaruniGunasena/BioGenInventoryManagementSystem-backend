package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OrderDetailsReportStrategy implements ReportStrategy {

    @Autowired
    private ReportRepository reportRepo;

    @Override
    public String getReportIdentifier() {
        return "ORDER_DETAILS";
    }

    @Override
    public String getReportName() {
        return "Order Item Specification Report";
    }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        String orderIdStr = params.get("orderId");
        if (orderIdStr == null) {
            throw new RuntimeException("OrderId is required for this report");
        }
        return reportRepo.getOrderDetails(Long.parseLong(orderIdStr));
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }

    @Override
    public List<String> getColumnOrder() {

        return List.of("Product","Qty","Price","SubTotal");
    }
}