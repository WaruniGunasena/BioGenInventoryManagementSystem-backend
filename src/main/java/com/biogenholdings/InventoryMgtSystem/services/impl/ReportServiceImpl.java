package com.biogenholdings.InventoryMgtSystem.services.impl;


import com.biogenholdings.InventoryMgtSystem.services.IReportService; // Your Interface

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.Reports.utils.PdfGeneratorUtility;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements IReportService {

    private final Map<String, ReportStrategy> reportStrategies;

    // FIX: This name MUST match the class name "ReportServiceImpl"
    public ReportServiceImpl(List<ReportStrategy> strategyList) {
        this.reportStrategies = strategyList.stream()
                .collect(Collectors.toMap(
                        s -> s.getReportIdentifier().toUpperCase(),
                        Function.identity()
                ));
    }

    @Override
    public byte[] generatePdfReport(String reportType, Map<String, String> params) {
        ReportStrategy strategy = reportStrategies.get(reportType.toUpperCase());

        if (strategy == null) {
            throw new RuntimeException("Report type '" + reportType + "' not found!");
        }

        List<Map<String, Object>> data = strategy.getReportData(params);
        return PdfGeneratorUtility.createPdf(strategy.getReportName(), data);
    }

}