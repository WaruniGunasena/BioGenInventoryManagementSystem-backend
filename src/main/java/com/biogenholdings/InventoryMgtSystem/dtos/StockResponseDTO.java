package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockResponseDTO {

    private Long stockId;
    private Long productId;
    private String productName;
    private Integer totalQuantity;
    private BigDecimal sellingPrice;
    private Integer minimumStockLevel;
    private Integer reorderLevel;
}