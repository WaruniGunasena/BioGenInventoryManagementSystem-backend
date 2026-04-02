package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.services.CashFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/cashflow")
@RequiredArgsConstructor

public class CashFlowController {

    private final CashFlowService cashFlowService;

    @GetMapping("/pending")
    public ResponseEntity<Response> getPendingCashFlow(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Response pendingCashFlow = cashFlowService.getPendingCredits(startDate, endDate);
        return ResponseEntity.ok(pendingCashFlow);
    }

    @GetMapping("/completed")
    public ResponseEntity<Response> getCompletedCashFlow(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Response completedCashFlow = cashFlowService.getCompletedCashFlow(startDate, endDate);
        return ResponseEntity.ok(completedCashFlow);
    }
}
