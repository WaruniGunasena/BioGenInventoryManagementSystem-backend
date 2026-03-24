package com.biogenholdings.InventoryMgtSystem.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "grn_payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class GRNPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private BigDecimal grandTotal;

    private String bank;

    private String chequeNumber;

    @Column(name = "due_balance")
    private BigDecimal dueBalance;

    private LocalDate chequeIssueDate;

    private LocalDate chequeDueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GRN grn;

    private Long createdBy;

    private LocalDateTime createdAt;

    private Long updatedBy;

    private LocalDateTime updatedAt;

    private Boolean isDeleted = false;

    private Long deletedBy;

    private LocalDateTime deletedAt;

    @Override
    public String toString() {
        return "GRNPayment{" +
                "id=" + id +
                ", amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", grandTotal=" + grandTotal +
                ", bank='" + bank + '\'' +
                ", chequeNumber='" + chequeNumber + '\'' +
                ", dueBalance=" + dueBalance +
                ", chequeIssueDate=" + chequeIssueDate +
                ", chequeDueDate=" + chequeDueDate +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", updatedBy=" + updatedBy +
                ", updatedAt=" + updatedAt +
                ", isDeleted=" + isDeleted +
                ", deletedBy=" + deletedBy +
                ", deletedAt=" + deletedAt +
                '}';
    }

}
