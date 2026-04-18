package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.dtos.ProductReturnRequestDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.services.ProductReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ProductReturnController {
    private final ProductReturnService productReturnService;

    /**
     * Process a new return from a customer.
     * This will update stock (if reusable) and reduce customer debt.
     */
    @PostMapping("/process")
    public ResponseEntity<Response> processReturn(@RequestBody ProductReturnRequestDTO request) {

        return ResponseEntity.ok(productReturnService.processProductReturn(request));
    }

    @GetMapping("/allReturns")
    public ResponseEntity<Response> getAllReturns(@RequestParam int page, @RequestParam int size){
        return  ResponseEntity.ok(productReturnService.findAllReturns(page,size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getReturnById(@PathVariable String id){
        return ResponseEntity.ok(productReturnService.findReturnById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Response> getCustomerreturnSummary(@PathVariable Long customerId){
        return ResponseEntity.ok(productReturnService.getCustomerReturnSummary(customerId));
    }

    @PostMapping("/delete")
    public ResponseEntity<Response> deleteReturnInvoice(@RequestParam String returnInvoiceId,
                                                        @RequestParam Long userId ){
        return ResponseEntity.ok(productReturnService.deleteReturnInvoice(returnInvoiceId,userId));
    }

    @PostMapping("/approveRtnInvoice")
    public ResponseEntity<Response> approveRtnInvoice(@RequestParam String returnInvoiceId,
                                                      @RequestParam Long userId){
        return ResponseEntity.ok(productReturnService.approveReturn(returnInvoiceId,userId));

    }
}
