package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.services.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private IReportService reportService;

    @GetMapping("/{reportType}/download")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable String reportType,
            @RequestParam Map<String, String> params) {

        // The service will find the right strategy and generate the PDF
        byte[] pdfContent = reportService.generatePdfReport(reportType, params);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + reportType + ".pdf")
                .body(pdfContent);
    }
}