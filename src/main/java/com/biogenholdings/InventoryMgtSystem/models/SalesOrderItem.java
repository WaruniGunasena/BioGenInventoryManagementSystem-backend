package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_order_id", nullable = false)
    @ToString.Exclude
    private SalesOrder salesOrder;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    @Column(nullable = false)
    private BigDecimal sellingPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    private BigDecimal discountPercent;

    private BigDecimal discountedPrice;

    private String unit;

    @Override
    public String toString() {
        return "SalesOrderItem{" +
                "sellingPrice=" + sellingPrice +
                ", quantity=" + quantity +
                ", totalAmount=" + totalAmount +
                ", discountPercent=" + discountPercent +
                ", discountedPrice=" + discountedPrice +
                ", unit='" + unit + '\'' +
                ", id=" + id +
                '}';
    }
}
