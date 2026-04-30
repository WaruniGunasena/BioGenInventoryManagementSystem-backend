package com.biogenholdings.InventoryMgtSystem.services.impl;


import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import com.biogenholdings.InventoryMgtSystem.services.IReportService; // Your Interface

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.Reports.utils.PdfGeneratorUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {

    private final Map<String, ReportStrategy> reportStrategies;
    private final ReportRepository reportRepository;

    // FIX: This name MUST match the class name "ReportServiceImpl"
    @Autowired
    public ReportServiceImpl(List<ReportStrategy> strategyList, ReportRepository reportRepository) {
        this.reportStrategies = strategyList.stream()
                .collect(Collectors.toMap(
                        s -> s.getReportIdentifier().toUpperCase(),
                        Function.identity()
                ));
        this.reportRepository = reportRepository;
    }

    @Override
    public byte[] generatePdfReport(String reportType, Map<String, String> params) {
        ReportStrategy strategy = reportStrategies.get(reportType.toUpperCase());

        if (strategy == null) {
            throw new RuntimeException("Report type '" + reportType + "' not found!");
        }

        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        String dateLabel = params.get("date"); // For daily reports using 'date' param

        String dateDisplay;
        if(Objects.equals(startDate, endDate) && startDate != null){
            dateDisplay = "Date: " + startDate;
        } else if (startDate != null && endDate != null) {
            dateDisplay = "Period: " + startDate + " to " + endDate;
        } else // Fallback to Today
            if (startDate != null) {
            dateDisplay = "Date: " + startDate;
        } else dateDisplay = "Date: " + Objects.requireNonNullElseGet(dateLabel, () -> LocalDate.now().toString());

        String extraHeaderInfo = "";
        if (reportType.equalsIgnoreCase("INDIVIDUAL_CUSTOMER")) {
            // Fetch specific customer info to put in the PDF subtitle
            Map<String, Object> customer = reportRepository.getCustomerDetailHeader(Long.parseLong(params.get("customerId")));
            String creditPeriod = String.valueOf(customer.get("credit_period"));
            String lastTxDate = String.valueOf(customer.get("Last_Tx_Date"));
            String lastTxAmount = String.valueOf(customer.get("Last_Tx_Amount"));
            String duePart;

            if ("CASH".equalsIgnoreCase(creditPeriod)) {
                duePart = " | Total Due: Rs.0";
            } else {
                duePart = " Days | Total Due: Rs." + customer.get("due_balance");
            }

            extraHeaderInfo =
                    "Customer: " + customer.get("name") +
                            " | Address: " + customer.get("address") +
                            "\nCredit Period: " + creditPeriod +
                            duePart + "\nLast Transaction: Rs. " + lastTxAmount + " on " +lastTxDate  ;
        }
        else{
            extraHeaderInfo = dateDisplay;
        }

        List<Map<String, Object>> data = strategy.getReportData(params);
        return PdfGeneratorUtility.createPdf(
                "BioGenHoldings Pvt Ltd",
                strategy.getReportName(),
                extraHeaderInfo,
                data,
                strategy.getOrientation(data),
                strategy.getColumnOrder(),
                strategy.addGrandTotal());
    }

    @Override
    public List<Map<String, Object>> getReportRawData(String reportType, Map<String, String> params) {
        ReportStrategy strategy = reportStrategies.get(reportType.toUpperCase());
        if (strategy == null) throw new RuntimeException("Dashboard component not found");

        return strategy.getReportData(params);
    }

}