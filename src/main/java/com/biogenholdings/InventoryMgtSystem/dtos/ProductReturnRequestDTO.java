package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReturnRequestDTO {
    private Long salesOrderId;
    private String remarks;
    private Long userId;
    private List<ReturnItemRequestDTO> items;
}

