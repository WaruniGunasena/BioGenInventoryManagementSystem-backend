package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_stock",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "batch_number"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="product_id", nullable = false)
    private Product product;

    @Column(name="batch_number", nullable = false)
    private String batchNumber;

    @Column(name="mfg_date")
    private LocalDate mfgDate;

    @Column(name="exp_date", nullable = false)
    private LocalDate expDate;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name="purchase_price", nullable = false)
    private BigDecimal purchasePrice;

    @Column(name="selling_price")
    private BigDecimal sellingPrice;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
    }
}