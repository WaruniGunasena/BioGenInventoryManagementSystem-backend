package com.biogenholdings.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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