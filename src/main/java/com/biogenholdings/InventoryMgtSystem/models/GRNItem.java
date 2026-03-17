package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private Integer bonus;

    private String packSize;

    @Column(name = "sp_percentage")
    private BigDecimal SellingPricePercentage;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name="total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "dis_percentage")
    private BigDecimal discountPercentage;

    @Column(name = "mrp_value")
    private BigDecimal mrpValue;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}