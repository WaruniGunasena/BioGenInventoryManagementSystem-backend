package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.enums.SalesOrderStatus;
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

    @DeleteMapping("/softDelete")
    public ResponseEntity<Response> softDeleteSalesOrders(@RequestParam Long salesOrderID,
                                                          @RequestParam Long userId){
        return ResponseEntity.ok(salesOrderService.softDeleteSalesOrder(salesOrderID,userId));
    }

    @PutMapping("/update")
    public ResponseEntity<SalesOrderResponseDTO> updateSalesOrder(@RequestParam Long salesOrderID,
                                                                  @RequestParam Long userID,
                                                                  @RequestBody SalesOrderRequestDTO salesOrderRequestDTO){
        return ResponseEntity.ok(salesOrderService.updateSalesOrder(salesOrderID,salesOrderRequestDTO,userID));
    }

    @PostMapping("/Approval")
    public ResponseEntity<Response> approveSalesOrder(@RequestParam Long userId,
                                                      @RequestParam SalesOrderStatus salesOrderStatus,
                                                      @RequestParam Long salesOrderId){
        return ResponseEntity.ok(salesOrderService.approveSalesOrder(salesOrderStatus,userId,salesOrderId));
    }

    @GetMapping("/getPendingOrderCount")
    public ResponseEntity<Long> getPendingOrderCount(){
        return ResponseEntity.ok(salesOrderService.pendingSalesOrderCount());
    }

    @PostMapping("/payment")
    public ResponseEntity<Response> createSalesOrderPayment(@RequestBody SalesOrderPaymentDTO dto) {
        Response response = salesOrderService.createSalesOrderPayment(dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
