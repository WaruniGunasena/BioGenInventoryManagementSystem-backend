package com.biogenholdings.InventoryMgtSystem.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDto {

    @NotNull(message = "User ID can not be blank")
    private Long userId;

    @NotBlank(message = "Password can not be blank")
    private String password;
}
