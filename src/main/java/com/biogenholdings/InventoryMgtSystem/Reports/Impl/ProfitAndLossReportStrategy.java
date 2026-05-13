package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProfitAndLossReportStrategy implements ReportStrategy {
    private final ReportRepository reportRepo;

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        // 1. Get Stock Value (Reusing your previous logic)
        List<Map<String, Object>> stockData = reportRepo.getStockValueReport();
        BigDecimal totalStockValue = stockData.stream()
                .map(row -> safeGetBigDecimal(row.get("Total_Amount")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Get Pending Metrics
        Map<String, Object> metrics = reportRepo.getFinancialOverviewMetrics();

        BigDecimal supplierPending = new BigDecimal(metrics.get("supplierPendingValue").toString());
        BigDecimal customerCheque = new BigDecimal(metrics.get("customerPendingCheque").toString());
        BigDecimal customerInvoice = new BigDecimal(metrics.get("customerPendingInvoice").toString());

        // Manual entry or from a settings table if implemented
        BigDecimal salesRepComm = new BigDecimal(metrics.get("salesRepCommission").toString());

        // 3. Construct the report rows to match Screenshot 2026-05-13 at 12.55.14.png
        Map<String, Object> row1 = new HashMap<>();
        row1.put("Label", "Credit");
        row1.put("Value", "");

        Map<String, Object> row = new HashMap<>();
        row.put("Label", "Supplier Pending Value");
        row.put("Value", supplierPending);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("Label", "Sales Rep Commission");
        row2.put("Value", salesRepComm);

        Map<String, Object> row3 = new HashMap<>();
        row3.put("Label", "Debit");
        row3.put("Value", "");

        Map<String, Object> row4 = new HashMap<>();
        row4.put("Label", "Customer Pending Cheque");
        row4.put("Value", customerCheque);


        Map<String, Object> row5 = new HashMap<>();
        row5.put("Label", "Customer Pending Invoice");
        row5.put("Value", customerInvoice);

        Map<String, Object> row6 = new HashMap<>();
        row6.put("Label", "Stock Value");
        row6.put("Value", totalStockValue);

        Map<String, Object> rowPNL = new HashMap<>();
        rowPNL.put("Label", "Profit Or Loss");
        rowPNL.put("Value", "");

        // Calculate Total Value (Assets - Liabilities)
        BigDecimal totalValue = totalStockValue.add(customerCheque).add(customerInvoice)
                .subtract(supplierPending).subtract(salesRepComm);

        Map<String, Object> row7 = new HashMap<>();
        row7.put("Label", "Total Value");
        row7.put("Value", totalValue);

        return List.of(row1,row, row2, row3, row4, row5, row6,rowPNL,row7);
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }

    @Override
    public List<String> getColumnOrder() {
        return List.of("Label", "Value");
    }

    @Override
    public String getReportName() { return "Profit and Loss Statement"; }
    @Override
    public String getReportIdentifier() { return "PROFIT_LOSS"; }

    private BigDecimal safeGetBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }
}
