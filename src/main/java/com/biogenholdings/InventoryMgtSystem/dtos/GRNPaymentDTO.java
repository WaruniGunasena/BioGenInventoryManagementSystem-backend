package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GRNPaymentDTO {

    private BigDecimal amount;
    private String bank;
    private String chequeDueDate;
    private String chequeIssueDate;
    private String chequeNumber;
    private BigDecimal grandTotal;
    private Long grnId;
    private String paymentMethod;
    private Long userId;
}
