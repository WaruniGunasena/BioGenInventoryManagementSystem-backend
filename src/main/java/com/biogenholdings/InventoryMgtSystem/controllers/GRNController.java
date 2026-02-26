package com.biogenholdings.InventoryMgtSystem.controllers;

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
}