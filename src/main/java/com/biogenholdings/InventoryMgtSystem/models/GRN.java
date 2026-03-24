package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grn")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GRN {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="grn_number", unique = true)
    private String grnNumber;

    @Column(name="invoice_number", nullable = false)
    private String invoiceNumber;

    @Column(name="grn_date", nullable = false)
    private LocalDate grnDate;

    @Column(name="grand_total", nullable = false)
    private BigDecimal grandTotal;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Builder.Default
    @OneToMany(mappedBy = "grn", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<GRNItem> items = new ArrayList<>();

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
    }

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "payment_status")
    private String paymentStatus;
}