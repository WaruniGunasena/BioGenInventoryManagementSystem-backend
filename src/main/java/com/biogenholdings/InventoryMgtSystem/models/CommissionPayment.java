package com.biogenholdings.InventoryMgtSystem.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CommissionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private BigDecimal grandTotal;

    @Column(name = "due_balance")
    private BigDecimal dueBalance;

    private String bank;

    private String chequeNumber;

    private LocalDate chequeIssueDate;

    private LocalDate chequeDueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MonthlyCommissionInvoice invoice;

    private Long createdBy;

    private LocalDateTime createdAt;

    private Long updatedBy;

    private LocalDateTime updatedAt;

    private Boolean isDeleted = false;

    private Long deletedBy;

    private LocalDateTime deletedAt;
}
