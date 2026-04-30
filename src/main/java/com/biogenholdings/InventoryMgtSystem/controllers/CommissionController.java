package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.dtos.CommissionPaymentDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.services.CommissionService;
import com.biogenholdings.InventoryMgtSystem.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/commissions")
@RequiredArgsConstructor

public class CommissionController {

    private final CommissionService commissionService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Response> getAdminCommissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(commissionService.getAllMonthlyInvoices(page, size));
    }

    @GetMapping("/my")
    public ResponseEntity<Response> getMyCommissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Long currentUserId = userService.getCurrentLoggedInUser().getId();

        Response response = commissionService.getMyCommissions(currentUserId, page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<Response> getMyCommissionHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(commissionService.getMyCommissionHistory(userId, page, size));
    }

    @PostMapping("/payment")
    public ResponseEntity<Response> submitCommissionPayment(@RequestBody CommissionPaymentDTO dto) {
        Response response = commissionService.submitCommissionPayment(dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/details/{invoiceNumber}")
    public ResponseEntity<Response> getCommissionInvoiceDetails(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(commissionService.getCommissionInvoiceDetails(invoiceNumber));
    }

    @GetMapping("/my-reversals")
    public ResponseEntity<Response> getMyCommissionReversals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Long currentUserId = userService.getCurrentLoggedInUser().getId();

        Response response = commissionService.getMyCommissionReversals(currentUserId, page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}