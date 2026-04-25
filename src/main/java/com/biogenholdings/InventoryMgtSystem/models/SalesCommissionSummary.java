package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales_commission_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SalesCommissionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String salesOrderId;

    private Long salesRepId;

    private String invoiceNumber;

    private LocalDateTime invoiceDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal CommissionableAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal ReturnCommission;

    @Column(precision = 15, scale = 2)
    private BigDecimal TotalCommission;

    @OneToMany(mappedBy = "summary", cascade = CascadeType.ALL)
    private List<SalesCommissionItem> items;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
