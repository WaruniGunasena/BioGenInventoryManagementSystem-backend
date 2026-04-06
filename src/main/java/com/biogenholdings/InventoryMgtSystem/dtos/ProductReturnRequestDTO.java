package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.Data;

import java.util.List;

@Data
public class ProductReturnRequestDTO {
    private Long salesOrderId;
    private String remarks;
    private List<ReturnItemRequestDTO> items;
}

