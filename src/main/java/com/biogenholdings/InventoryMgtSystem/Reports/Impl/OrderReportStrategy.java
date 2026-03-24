package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OrderReportStrategy implements ReportStrategy {

    @Autowired
    private ReportRepository reportRepo;

    @Override
    public String getReportIdentifier() {
        return "ORDER_REPORT";
    }

    @Override
    public String getReportName() {
        return "Order Summary Report";
    }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        // Retrieve status from params (PENDING, APPROVED, DELIVERED, CANCELLED)
        // If no status is passed, it returns all orders (Report 2.1)
        String status = params.get("status");
        return reportRepo.getOrdersByStatus(status);
    }
}
