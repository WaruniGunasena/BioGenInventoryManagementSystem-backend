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
    private BigDecimal totalAmount;
}