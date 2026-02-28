package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class GRNResponseDTO {

    private Long id;
    private String grnNumber;
    private String invoiceNumber;
    private LocalDate grnDate;
    private BigDecimal grandTotal;

    private SupplierDTO supplier;

    private List<GRNItemResponseDTO> items;
}