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
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class DebitDTO {

    private Long salesOrderId;
    private String customer;
    private BigDecimal amount;
    private LocalDate date;
    private String invoiceNumber;
}
