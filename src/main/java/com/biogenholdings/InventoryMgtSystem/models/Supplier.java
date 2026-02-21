package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "suppliers")
@Data
@Builder

public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Column(name = "contact_person")
    private String contactPerson;

    @NotBlank(message = "PhoneNumber is required")
    @Column(name = "phone_number")
    private String phoneNumber;

    private String creditPeriod;

    private String email;

    private String address;

    @Column(name = "postal_code")
    private String postalCode;
}
