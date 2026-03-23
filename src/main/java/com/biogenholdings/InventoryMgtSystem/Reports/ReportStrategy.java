package com.biogenholdings.InventoryMgtSystem.Reports;

import java.util.List;
import java.util.Map;

public interface ReportStrategy {
    // This identifies which report the class handles (e.g., "LOW_STOCK")
    String getReportIdentifier();

    // The display name for the PDF header
    String getReportName();

    // The actual data fetching logic
    List<Map<String, Object>> getReportData(Map<String, String> params);
}