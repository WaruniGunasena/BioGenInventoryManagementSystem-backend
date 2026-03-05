package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderItemRequestDTO {
    private Long productId;
    private Integer quantity;
    private BigDecimal sellingPrice;
    private BigDecimal totalAmount;
    private BigDecimal discountPercent;
    private BigDecimal discountedPrice;
    private String unit;
}