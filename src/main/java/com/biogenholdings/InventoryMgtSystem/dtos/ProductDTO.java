package com.biogenholdings.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class ProductDTO {

    private Long id;

    private Long categoryId;

    private String categoryName;

    private String name;

    private String unit;

    private String packSize;

    private Integer minimumStockLevel;

    private Integer reorderLevel;

    private String itemCode;

    private String description;

    private Integer openingBalance;

    private BigDecimal sRepCommissionRate;

    private BigDecimal sellingPrice;

    private BigDecimal mrp;

    private String imageUrl;

    private LocalDateTime createdAt;

    private Boolean isDeleted;

    private UserDTO deletedBy;

    private LocalDateTime deletedAt;

}
