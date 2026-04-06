package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "product_return_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_return_id", nullable = false)
    private ProductReturn productReturn;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPriceAtReturn; // Original selling price from SalesOrderItem

    @Column(nullable = false)
    private BigDecimal subTotal; // quantity * unitPriceAtReturn

    @Column(nullable = false)
    private Boolean isReusable; // True = Restock, False = Damaged/Discard

    private String returnReason; // e.g., "Expired", "Damaged", "Wrong Item"

    @Column(nullable = false)
    private BigDecimal commissionReversalAmount; // Calculated: (subTotal * sRepCommissionRate / 100)
}
