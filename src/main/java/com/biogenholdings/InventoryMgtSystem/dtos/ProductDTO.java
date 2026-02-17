package com.biogenholdings.InventoryMgtSystem.dtos;

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

public class ProductDTO {

    private Long id;

    private Long categoryId;

    private String name;

    private BigDecimal sellingPrice;

    private Integer minimumStockLevel;

    private Integer reorderLevel;

    private String description;

    private String imageUrl;

    private LocalDateTime createdAt;

}
