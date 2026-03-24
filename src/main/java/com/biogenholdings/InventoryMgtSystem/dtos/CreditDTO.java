package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CreditDTO {

    private Long grnId;
    private String supplier;
    private BigDecimal amount;
    private LocalDate date;
    private String invoiceNumber;
}
