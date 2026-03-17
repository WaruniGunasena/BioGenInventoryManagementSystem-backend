package com.biogenholdings.InventoryMgtSystem.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class GRNRequestDTO {

    private Long supplierId;

    @NotNull(message = "GRN date is required")
    private LocalDate date;

    private String invoiceNumber;
    private BigDecimal grandTotal;
    private Long userId;
    private List<GRNItemDTO> items;
}