package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GRNItemDTO {

    private Long productId;
    private String batchNumber;
    private LocalDate mfgDate;
    private LocalDate expDate;
    private BigDecimal purchasePrice;
    private Integer quantity;
    private BigDecimal totalAmount;
}