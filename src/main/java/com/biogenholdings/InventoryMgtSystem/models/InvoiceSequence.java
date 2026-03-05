package com.biogenholdings.InventoryMgtSystem.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invoice_sequence")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceSequence {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long lastNumber;
}