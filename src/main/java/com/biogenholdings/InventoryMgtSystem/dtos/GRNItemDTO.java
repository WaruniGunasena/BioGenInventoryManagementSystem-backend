package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GRNItemDTO {

    private Long id;
    private Long productId;
    private String batchNumber;
    private LocalDate mfgDate;
    private LocalDate expDate;
    private BigDecimal purchasePrice;
    private Integer quantity;
    private Integer bonus;
    private String packSize;
    private BigDecimal totalAmount;
    private BigDecimal sellingPricePercentage;
    private BigDecimal discountValue;
    private BigDecimal mrpValue;
    private BigDecimal discountPercentage;
}