package com.biogenholdings.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
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

    private Integer minimumStockLevel;

    private Integer reorderLevel;

    private String description;

    private String imageUrl;

    private LocalDateTime createdAt;

}
