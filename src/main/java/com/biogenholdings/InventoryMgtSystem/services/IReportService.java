package com.biogenholdings.InventoryMgtSystem.services;

import java.util.List;
import java.util.Map;

public interface IReportService {
    /**
     * Finds the appropriate strategy for the report type and generates a PDF.
     * @param reportType The unique identifier for the report (e.g., "DAILY_SALES")
     * @param params Query parameters for filtering (date, customerId, etc.)
     * @return byte array of the generated PDF
     */
    byte[] generatePdfReport(String reportType, Map<String, String> params);

    // NEW: For Dashboards/JSON
    List<Map<String, Object>> getReportRawData(String reportType, Map<String, String> params);
}