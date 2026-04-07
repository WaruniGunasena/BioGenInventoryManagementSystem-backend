package com.biogenholdings.InventoryMgtSystem.Reports.utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PdfGeneratorUtility {

    public static byte[] createPdf(String title, List<Map<String, Object>> data) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Add Title
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            fontTitle.setSize(18);
            Paragraph para = new Paragraph(title, fontTitle);
            para.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(para);
            document.add(Chunk.NEWLINE);

            if (data.isEmpty()) {
                document.add(new Paragraph("No data available for this report."));
            } else {
                // Determine columns from the first row keys
                Set<String> keys = data.get(0).keySet();
                PdfPTable table = new PdfPTable(keys.size());
                table.setWidthPercentage(100);

                // Add Table Headers
                for (String key : keys) {
                    PdfPCell cell = new PdfPCell(new Phrase(key.replace("_", " ").toUpperCase()));
                    cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                    table.addCell(cell);
                }

                // Add Data Rows
                for (Map<String, Object> row : data) {
                    for (String key : keys) {
                        table.addCell(String.valueOf(row.get(key) != null ? row.get(key) : ""));
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
}