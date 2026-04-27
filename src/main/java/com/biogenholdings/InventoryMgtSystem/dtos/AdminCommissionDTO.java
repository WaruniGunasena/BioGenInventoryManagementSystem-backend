package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AdminCommissionDTO {

    private Long id;
    private String invoiceNumber;
    private String monthYear;
    private String salesRep;
    private BigDecimal monthlyCommissionAmount;
    private String paymentStatus;
    private BigDecimal dueBalance;
    private BigDecimal totalPaid;
}
