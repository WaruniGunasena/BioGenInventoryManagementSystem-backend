package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class GRNRequestDTO {

    private Long supplierId;
    private LocalDate date;
    private String invoiceNumber;
    private BigDecimal grandTotal;
    private Long userId;
    private List<GRNItemDTO> items;
}