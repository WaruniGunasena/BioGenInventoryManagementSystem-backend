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
public class SalesOrderItemResponseDTO {
    private Long id;

    private Long productId;
    private String productName;

    private Integer quantity;
    private BigDecimal sellingPrice;
    private BigDecimal totalAmount;

    private BigDecimal discountPercent;
    private BigDecimal discountedPrice;

    private String unit;

    private ProductDTO product;
}
