package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StockMovementProductWiseReportStrategy implements ReportStrategy {

    final private ReportRepository reportRepo;
    @Override public String getReportIdentifier() { return "STOCK_MOVEMENT_PRODUCT_WISE"; }
    @Override public String getReportName() { return "Stock In/Out Movement Log Product Wise"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        LocalDate start = LocalDate.parse(params.get("start"));
        LocalDate end = LocalDate.parse(params.get("end"));
        Long productId = Long.parseLong(params.get("productId"));

        // This 'rawData' is immutable (TupleBackedMap)
        List<Map<String, Object>> rawData = reportRepo.getProductWiseMovementLogWithLiveBalance(start, end, productId);

        // We create a new list to hold mutable HashMaps
        List<Map<String, Object>> mutableData = new ArrayList<>();

        for (Map<String, Object> row : rawData) {
            // Create a new mutable HashMap from the immutable row
            Map<String, Object> mutableRow = new HashMap<>(row);
            mutableData.add(mutableRow);
        }

        return mutableData;
    }

    @Override
    public List<String> getColumnOrder() {
        return List.of("Date", "Item_code", "Product_Name", "TYPE", "REF_NO_GRN_NO", "QTY_IN", "QTY_OUT", "BALANCE");
    }

    @Override
    public Boolean addGrandTotal(){
        return Boolean.TRUE;
    }

    @Override
    public String getOrientation(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return "portrait";

        int columnCount = data.getFirst().size();

        return columnCount > 6 ? "landscape" : "portrait";
    }
}
