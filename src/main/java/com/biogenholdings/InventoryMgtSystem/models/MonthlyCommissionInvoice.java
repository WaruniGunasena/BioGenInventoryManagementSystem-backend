package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_commission_invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class MonthlyCommissionInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "commission_invoice_number")
    private String commissionInvoiceNumber; // e.g., STMT-2026-04-REP001

    private Long salesRepId;

    private String monthYear; // "2026-04"

    private BigDecimal MonthlyCommission;

    private String payoutStatus; // UNPAID, PAID, PROCESSING

    private LocalDateTime generatedDate;

    private LocalDateTime paidDate;
}
