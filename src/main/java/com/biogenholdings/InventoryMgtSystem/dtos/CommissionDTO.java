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

public class CommissionDTO {

    private String commission_invoice_number;
    private String salesRep;
    private BigDecimal net_payout;
    private String month_year;
    private LocalDate paid_date;
}
