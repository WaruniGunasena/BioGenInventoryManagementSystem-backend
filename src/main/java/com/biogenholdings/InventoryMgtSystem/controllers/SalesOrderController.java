package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.dtos.SalesOrderRequestDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.SalesOrderResponseDTO;
import com.biogenholdings.InventoryMgtSystem.services.SalesOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping("/create")
    public ResponseEntity<SalesOrderResponseDTO> createSalesOrder(
            @RequestBody SalesOrderRequestDTO request) {

        SalesOrderResponseDTO response = salesOrderService.createSalesOrder(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Response> getPaginatedSalesOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        Response response = salesOrderService.getPaginatedSalesOrders(page, size);

        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
