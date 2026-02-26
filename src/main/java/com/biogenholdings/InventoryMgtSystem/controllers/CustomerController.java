package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.dtos.CustomerDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.enums.FilterEnum;
import com.biogenholdings.InventoryMgtSystem.services.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer")
public class CustomerController {

    private  final CustomerService customerService;

    @PostMapping("/add")
    public ResponseEntity<Response> createCustomer(@RequestBody @Valid CustomerDTO customerDTO){
        return ResponseEntity.ok(customerService.addCustomer(customerDTO));
    }

    @GetMapping("/getAllPaginated")
    public ResponseEntity<Response> getAllCustomersPaginated(@RequestParam(defaultValue = "0") Integer page,
                                                             @RequestParam(defaultValue = "5") Integer size,
                                                             @RequestParam FilterEnum filter){
        return ResponseEntity.ok(customerService.getAllCustomersPaginated(page,size,filter));
    }

    @GetMapping("/getAll")
    public ResponseEntity<Response> getAllCustomers(){
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getCustomerById(@PathVariable Long id){
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @PutMapping("/softDelete")
    public ResponseEntity<Response> softDeleteCustomer(@RequestParam Long customerId, @RequestParam Long userId){
        return ResponseEntity.ok(customerService.softDeleteCustomer(customerId,userId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updateCustomer(@PathVariable Long customerId,@RequestBody CustomerDTO customerDTO){
        return ResponseEntity.ok(customerService.updateCustomer(customerId,customerDTO));
    }

    @GetMapping("/search")
    public ResponseEntity<Response> searchCustomer(@RequestParam String name){
        return ResponseEntity.ok(customerService.searchCustomer(name));
    }
}
