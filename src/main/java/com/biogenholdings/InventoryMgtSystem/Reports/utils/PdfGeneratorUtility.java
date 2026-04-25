package com.biogenholdings.InventoryMgtSystem.Reports.utils;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public class PdfGeneratorUtility {

    public static byte[] createPdf(String companyName, String title, String date, List<Map<String, Object>> data, String orientation, List<String> columnOrder, Boolean addGrandTotal) {

        boolean isLandscape = "landscape".equalsIgnoreCase(orientation);
        Rectangle pageSize = isLandscape ? PageSize.A4.rotate() : PageSize.A4;

        Document document = new Document(pageSize, 30, 30, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Headers (Company, Title, Date)
            Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph companyPara = new Paragraph(companyName, companyFont);
            companyPara.setAlignment(Element.ALIGN_CENTER);
            document.add(companyPara);

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph para = new Paragraph(title, fontTitle);
            para.setAlignment(Element.ALIGN_CENTER);
            document.add(para);

            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph datePara = new Paragraph(date, dateFont);
            datePara.setAlignment(Element.ALIGN_CENTER);
            datePara.setSpacingAfter(20f);
            document.add(datePara);

            if (data == null || data.isEmpty()) {
                document.add(new Paragraph("No data available for this report."));
            } else {
                // Determine keys
                List<String> keys = (columnOrder != null && !columnOrder.isEmpty())
                        ? columnOrder
                        : new ArrayList<>(data.getFirst().keySet());

                PdfPTable table = new PdfPTable(keys.size());
                table.setWidthPercentage(100);

                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
                Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
                Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

                // Initialize totals map
                Map<String, BigDecimal> columnTotals = new HashMap<>();

                // 2. ADD HEADERS
                for (String key : keys) {
                    PdfPCell cell = new PdfPCell(new Phrase(toCamelCase(key), headerFont));
                    cell.setBackgroundColor(Color.LIGHT_GRAY);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(5);
                    table.addCell(cell);
                }

                // 3. ADD DATA ROWS & CALCULATE TOTALS

                for (Map<String, Object> row : data) {
                    for (String key : keys) {
                        Object value = row.get(key);
                        String cellText = (value != null) ? String.valueOf(value) : "";

                        // Summation Logic
                        if (value != null && (key.toLowerCase().contains("revenue") || key.toLowerCase().contains("amount"))) {
                            try {
                                BigDecimal numericValue = new BigDecimal(cellText);
                                columnTotals.put(key, columnTotals.getOrDefault(key, BigDecimal.ZERO).add(numericValue));
                            } catch (NumberFormatException e) {
                                // Not a valid number, skip summation
                            }
                        }

                        PdfPCell dataCell = new PdfPCell(new Phrase(cellText, dataFont));
                        dataCell.setPadding(4);

                        // Right-align numbers
                        if (key.toLowerCase().contains("revenue") || key.toLowerCase().contains("amount") || key.toLowerCase().contains("qty")) {
                            dataCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        }

                        table.addCell(dataCell);
                    }
                }

                // 4. ADD GRAND TOTAL ROW
                if(addGrandTotal) {
                    for (String key : keys) {
                        PdfPCell totalCell;
                        if (keys.indexOf(key) == 0) {
                            totalCell = new PdfPCell(new Phrase("GRAND TOTAL", footerFont));
                        } else if (columnTotals.containsKey(key)) {
                            totalCell = new PdfPCell(new Phrase(columnTotals.get(key).toString(), footerFont));
                            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        } else {
                            totalCell = new PdfPCell(new Phrase("", footerFont));
                        }

                        totalCell.setBackgroundColor(new Color(230, 230, 230));
                        totalCell.setPadding(5);
                        table.addCell(totalCell);
                    }
                }

                document.add(table);
            }

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        String[] words = input.split("[\\s_.]+");
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }
}