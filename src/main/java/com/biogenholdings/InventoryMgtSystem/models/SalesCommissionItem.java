package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_commission_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SalesCommissionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "commission_summary_id")
    private SalesCommissionSummary summary;

    private Long productId;

    private String productName;

    private Integer quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(precision = 5, scale = 4)
    private BigDecimal commissionRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal earnedCommission;

    private boolean isReturned = false;
}
