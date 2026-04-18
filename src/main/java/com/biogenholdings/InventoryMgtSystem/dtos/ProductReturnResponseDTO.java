package com.biogenholdings.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductReturnResponseDTO {
    // Header Information
    private String returnNumber;
    private LocalDateTime returnDate;
    private String originalInvoiceNumber;
    private String remarks;
    private String processedBy;

    // Customer Information
    private String customerName;
    private String customerAddress;
    private String customerPhone;

    // Sales Rep Information
    private String salesRepName;

    // Itemized List
    private List<ReturnItemDetailDTO> items;

    // Totals
    private BigDecimal totalReturnAmount;
    private BigDecimal totalCommissionReversal;
}