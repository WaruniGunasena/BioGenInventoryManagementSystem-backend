package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.Data;

@Data
public class ReturnItemRequestDTO {
    private Long productId;
    private Integer quantity;
    private Boolean isReusable;
    private String reason;
}
