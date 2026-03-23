package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.dtos.GRNPaymentDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.GRNRequestDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.services.GRNService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grn")
@RequiredArgsConstructor
@CrossOrigin
public class GRNController {

    private final GRNService grnService;

    @PostMapping("/create")
    public ResponseEntity<Response> createGRN(@RequestBody GRNRequestDTO dto) {
        Response response = grnService.createGRN(dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllGRNs() {
        Response response = grnService.getAllGRNs();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getGRNById(@PathVariable Long id) {
        Response response = grnService.getGRNById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping
    public ResponseEntity<Response> getPaginatedGRNs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        Response response = grnService.getPaginatedGRNs(page, size);

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updateGRN(
            @PathVariable Long id,
            @RequestBody GRNRequestDTO dto) {

        Response response = grnService.updateGRN(id, dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/softDelete")
    public ResponseEntity<Response> softDeleteGRN(
            @RequestParam Long id,
            @RequestParam Long userId) {

        Response response = grnService.softDeleteGRN(id, userId);

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/payment")
    public ResponseEntity<Response> createGRNPayment(@RequestBody GRNPaymentDTO dto) {
        Response response = grnService.createGRNPayment(dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}