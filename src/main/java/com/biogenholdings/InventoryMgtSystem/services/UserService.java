package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.models.User;

public interface UserService {

    Response registerUser(RegisterRequest registerRequest);

    Response registerEmployee(EmpRegisterRequest empRegisterRequest);

    Response loginUser(LoginRequest loginRequest);

    Response getAllUsers();

    Response getUserById(Long id);

    Response updateUser(Long id, UserDTO userDTO);

    Response deleteUser(Long id);

    User getCurrentLoggedInUser();

    Response resetTempPassword(ResetPasswordDto resetPasswordDto);


    Response generateTempPasswordForForgetPassword(String email);

}
