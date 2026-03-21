package com.biogenholdings.InventoryMgtSystem.dtos;

import com.biogenholdings.InventoryMgtSystem.enums.SalesOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderResponseDTO {

    private Long id;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String creditTerm;

    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal grandTotal;

    private CustomerDTO customer;

    private UserDTO user;

    private Long userId;

    private SalesOrderStatus status;

    private List<SalesOrderItemResponseDTO> items;
}