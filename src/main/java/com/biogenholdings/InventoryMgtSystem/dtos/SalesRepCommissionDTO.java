package com.biogenholdings.InventoryMgtSystem.dtos;

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

public class SalesRepCommissionDTO {

    private String invoiceNumber;
    private String customer;
    private LocalDate date;
    private BigDecimal commissionableAmount;
    private BigDecimal totalCommission;
}
