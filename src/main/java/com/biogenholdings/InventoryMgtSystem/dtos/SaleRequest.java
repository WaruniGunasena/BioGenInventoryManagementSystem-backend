package com.biogenholdings.InventoryMgtSystem.dtos;

import com.biogenholdings.InventoryMgtSystem.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class SaleRequest {

    @Positive(message = "product id is required")
    private Long productId;

    @Positive(message = "quantity is required")
    private Integer quantity;

    private Long salesRepId;

    private String description;
}
