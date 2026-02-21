package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.dtos.SupplierDTO;
import com.biogenholdings.InventoryMgtSystem.services.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/suppliers")

public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<Response> addSupplier(@RequestBody @Valid SupplierDTO supplierDTO){
        return ResponseEntity.ok(supplierService.addSupplier(supplierDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllSupplier(){
        return ResponseEntity.ok(supplierService.getAllSupplier());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getSupplierById(@PathVariable Long id){
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<Response> updateSupplier(@PathVariable Long id, @RequestBody SupplierDTO supplierDTO){
        return ResponseEntity.ok(supplierService.updateSupplier(id, supplierDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<Response> deleteSupplier(@PathVariable Long id){
        return ResponseEntity.ok(supplierService.deleteSupplier(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Response> searchSupplier(@RequestParam String searchKey){
        return ResponseEntity.ok(supplierService.searchSupplier(searchKey));
    }

}
