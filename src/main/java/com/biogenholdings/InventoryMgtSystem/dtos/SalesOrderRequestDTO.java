package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderRequestDTO {
    private Long customerId;
    private Long userId;
    private LocalDate date;
    private BigDecimal grandTotal;
    private List<SalesOrderItemRequestDTO> items;
}
