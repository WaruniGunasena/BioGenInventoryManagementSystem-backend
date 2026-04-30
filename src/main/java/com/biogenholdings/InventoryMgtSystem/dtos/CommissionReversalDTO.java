package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CommissionReversalDTO {

    private String invoiceNumber;
    private BigDecimal totalReturnAmount;
    private BigDecimal totalCommissionReversal;
    private LocalDateTime invoiceDate;
    private String salesRepName;
    private String customerName;
}
