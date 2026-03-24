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

public class DebitDTO {

    private Long grnId;
    private String customer;
    private BigDecimal amount;
    private LocalDate date;
}
