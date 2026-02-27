package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.dtos.EmpRegisterRequest;
import com.biogenholdings.InventoryMgtSystem.dtos.ResetPasswordDto;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.dtos.UserDTO;
import com.biogenholdings.InventoryMgtSystem.models.User;
import com.biogenholdings.InventoryMgtSystem.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO){
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteUser(@PathVariable Long id){
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentLoggedInUser(){
        return ResponseEntity.ok(userService.getCurrentLoggedInUser());
    }

    @PostMapping("/registerEmp")
    public ResponseEntity<Response> registerEmployee(@RequestBody @Valid EmpRegisterRequest empRegisterRequest){
        return ResponseEntity.ok(userService.registerEmployee(empRegisterRequest));
    }

    @PutMapping("/resetTempPassword")
    public ResponseEntity<Response> resetTempPassword(@RequestBody @Valid ResetPasswordDto resetPasswordDto){
        return ResponseEntity.ok(userService.resetTempPassword(resetPasswordDto));
    }

    @PostMapping("/forgetPassword/{email:.+}")
    public ResponseEntity<Response> generateTempPasswordForForgetPassword(@PathVariable String email){
        return ResponseEntity.ok(userService.generateTempPasswordForForgetPassword(email));
    }





}
