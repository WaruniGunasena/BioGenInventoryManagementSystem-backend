package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_returns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String returnNumber; // e.g., RET-2024-001 for auditing

    @ManyToOne
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder; // Link to original invoice

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "sales_rep_id", nullable = false)
    private User salesRep; // The 'User' who handled the original sale

    @Column(nullable = false)
    private LocalDateTime returnDate;

    @Column(nullable = false)
    private BigDecimal totalReturnAmount; // Sum of all returned items' values

    @Column(nullable = false)
    private BigDecimal totalCommissionReversal; // Amount to subtract from Rep's earnings

    private String remarks; // General notes for the return

    @OneToMany(mappedBy = "productReturn", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ProductReturnItem> returnItems = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy; // The user who processed the return
}