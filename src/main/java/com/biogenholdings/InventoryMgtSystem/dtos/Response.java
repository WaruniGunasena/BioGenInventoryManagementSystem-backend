package com.biogenholdings.InventoryMgtSystem.dtos;

import com.biogenholdings.InventoryMgtSystem.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class Response {

    private int status;
    private String message;

    private String token;
    private UserRole role;
    private String expirationTime;

    private Integer totalPages;
    private Long totalElements;

    private UserDTO user;
    private List<UserDTO> users;

    private SupplierDTO supplier;
    private List<SupplierDTO> suppliers;

    private CategoryDTO category;
    private List<CategoryDTO> categories;

    private ProductDTO product;
    private List<ProductDTO> products;

    private SaleDTO sale;
    private List<SaleDTO> sales;

    private GRNResponseDTO grn;
    private List<GRNResponseDTO> grnList;
    private CustomerDTO customer;
    private List<CustomerDTO> customers;

    private final LocalDateTime timestamp = LocalDateTime.now();

}
