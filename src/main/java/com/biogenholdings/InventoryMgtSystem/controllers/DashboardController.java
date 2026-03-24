package com.biogenholdings.InventoryMgtSystem.controllers;


import com.biogenholdings.InventoryMgtSystem.dtos.DashboardStatsDTO;
import com.biogenholdings.InventoryMgtSystem.services.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        // One clean call to the service
        return ResponseEntity.ok(dashboardService.getDashboardLiveStats());
    }
}
