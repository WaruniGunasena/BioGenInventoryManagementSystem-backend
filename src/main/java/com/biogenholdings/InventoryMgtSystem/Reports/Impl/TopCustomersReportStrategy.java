package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TopCustomersReportStrategy implements ReportStrategy {

    @Autowired
    private ReportRepository reportRepo;

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
}
