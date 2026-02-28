package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "grn_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GRNItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="grn_id", nullable = false)
    private GRN grn;

    @ManyToOne
    @JoinColumn(name="product_id", nullable = false)
    private Product product;

    @Column(name="batch_number", nullable = false)
    private String batchNumber;

    @Column(name="mfg_date")
    private LocalDate mfgDate;

    @Column(name="exp_date", nullable = false)
    private LocalDate expDate;

    @Column(name="purchase_price", nullable = false)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name="total_amount", nullable = false)
    private BigDecimal totalAmount;
}