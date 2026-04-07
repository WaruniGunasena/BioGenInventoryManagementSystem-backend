package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomerBalanceReportStrategy implements ReportStrategy {

    final private ReportRepository reportRepo;
    @Override public String getReportIdentifier() { return "CUSTOMER_BALANCE"; }
    @Override public String getReportName() { return "Customer Credit Balance Summary"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        return reportRepo.getCustomerBalanceReport();
    }
}
