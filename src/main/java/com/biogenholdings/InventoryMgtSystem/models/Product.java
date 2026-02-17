package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
@Data
@Builder

public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @PositiveOrZero(message = "selling price must be a positive value")
    @Column(name = "selling_price")
    private BigDecimal sellingPrice;

    @Min(value = 0, message = "minimum stock quantity can not be negative")
    @Column(name = "minimum_stock_level")
    private Integer minimumStockLevel;

    @Min(value = 0, message = "reorder level can not be negative")
    @Column(name = "reorder_level")
    private Integer reorderLevel;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;   // or imagePath

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sellingPrice=" + sellingPrice +
                ", minimumStockLevel=" + minimumStockLevel +
                ", reorderLevel=" + reorderLevel +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
