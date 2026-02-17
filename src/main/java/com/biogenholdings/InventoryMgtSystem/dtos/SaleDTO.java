package com.biogenholdings.InventoryMgtSystem.dtos;

import com.biogenholdings.InventoryMgtSystem.enums.SaleStatus;
import com.biogenholdings.InventoryMgtSystem.enums.SaleType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class SaleDTO {

    private Long id;

    private Integer totalProducts;

    private BigDecimal totalPrice;

    private SaleType saleType;

    private SaleStatus status;

    private String description;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private UserDTO user;

    private SupplierDTO supplier;


}
