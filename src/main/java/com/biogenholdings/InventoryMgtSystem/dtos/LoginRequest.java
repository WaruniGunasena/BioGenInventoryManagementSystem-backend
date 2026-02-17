package com.biogenholdings.InventoryMgtSystem.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.internal.util.StringHelper;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class LoginRequest {

    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "password is required")
    private  String password;
}
