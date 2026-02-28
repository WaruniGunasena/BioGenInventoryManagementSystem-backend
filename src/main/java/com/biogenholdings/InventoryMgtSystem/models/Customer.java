package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name= "customers")
@Data
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @NotBlank(message = "Name can not be blank")
    private String name;

    @Column(unique = true)
    @NotBlank(message = "Email is required")
    private String email;

    private String contact_No;

    private String address;

    private String province;

    @Column(name = "postal_code")
    private String postalCode;

    private Boolean isDeleted;

    @ManyToOne
    private User deletedBy;

    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Customer{" +
                "Id=" + Id +
                ", Name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", contact_No='" + contact_No + '\'' +
                ", address='" + address + '\'' +
                ", province='" + province + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
