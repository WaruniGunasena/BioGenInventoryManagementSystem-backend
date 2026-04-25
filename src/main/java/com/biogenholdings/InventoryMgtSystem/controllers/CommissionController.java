package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.services.CommissionService;
import com.biogenholdings.InventoryMgtSystem.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commissions")
@RequiredArgsConstructor

public class CommissionController {

    private final CommissionService commissionService;
    private final UserService userService;

    @GetMapping("/my")
    public ResponseEntity<Response> getMyCommissions() {

        Long currentUserId = userService.getCurrentLoggedInUser().getId();

        Response response = commissionService.getMyCommissions(currentUserId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}