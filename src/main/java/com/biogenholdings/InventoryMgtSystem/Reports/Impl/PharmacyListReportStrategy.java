package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PharmacyListReportStrategy implements ReportStrategy {
    @Autowired
    private ReportRepository reportRepo;

    @Override public String getReportIdentifier() { return "PHARMACY_LIST"; }
    @Override public String getReportName() { return "Registered Pharmacies List"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        return reportRepo.getPharmacyList();
    }
}