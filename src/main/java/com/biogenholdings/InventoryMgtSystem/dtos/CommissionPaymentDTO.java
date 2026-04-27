package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CommissionPaymentDTO {

    private BigDecimal amount;
    private String paymentMethod;
    private Long invoiceId;
    private String bank;
    private String chequeNumber;
    private String chequeIssueDate;
    private String chequeDueDate;
    private Long userId;
}
