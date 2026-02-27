package com.biogenholdings.InventoryMgtSystem.dtos;

import com.biogenholdings.InventoryMgtSystem.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class EmpRegisterRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "email is required")
    private String email;

    private String address;

    private String hiringDate;

    //@NotBlank(message = "phoneNumber is required")
    private String phoneNumber;

    //@NotBlank(message = "nicNumber is required")
    private String nicNumber;

    private UserRole role;
}