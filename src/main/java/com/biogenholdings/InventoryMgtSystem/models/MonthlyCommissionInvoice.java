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
    private String commissionInvoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_rep_id")
    private User salesRep;

    private String monthYear;

    private BigDecimal MonthlyCommission;

    private String payoutStatus;

    private LocalDateTime generatedDate;

    private LocalDateTime paidDate;
}
