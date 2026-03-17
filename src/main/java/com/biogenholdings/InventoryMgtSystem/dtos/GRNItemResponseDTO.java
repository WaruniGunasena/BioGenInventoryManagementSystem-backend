package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class GRNItemResponseDTO {

    private Long id;
    private ProductDTO product;
    private String batchNumber;
    private LocalDate mfgDate;
    private LocalDate expDate;
    private BigDecimal purchasePrice;
    private Integer quantity;
    private Integer bonus;
    private String packSize;
    private BigDecimal SellingPricePercentage;
    private BigDecimal totalAmount;
    private BigDecimal discountValue;
    private BigDecimal discountPercentage;
    private BigDecimal mrpValue;
}