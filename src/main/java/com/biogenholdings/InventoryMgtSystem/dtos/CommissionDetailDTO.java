package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CommissionDetailDTO {

    private String invoiceNumber;
    private Double commissionableAmount;
    private Double totalCommission;
    private LocalDateTime invoiceDate;
    private String salesRepName;
    private String customerName;
}
