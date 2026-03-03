package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.dtos.StockResponseDTO;
import com.biogenholdings.InventoryMgtSystem.enums.FilterEnum;
import com.biogenholdings.InventoryMgtSystem.services.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor

public class StockController {

    private final StockService stockService;

    @GetMapping("/all")
    public ResponseEntity<List<StockResponseDTO>> getAllStock() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/search")
    public ResponseEntity<Response> searchStock(@RequestParam String searchKey){
        return ResponseEntity.ok(stockService.searchStock(searchKey));
    }

    @GetMapping
    public ResponseEntity<Response> paginatedResults(@RequestParam(defaultValue = "0") Integer page,
                                                     @RequestParam(defaultValue = "5") Integer size,
                                                     @RequestParam(defaultValue = "ASC") FilterEnum filter){
        return ResponseEntity.ok(stockService.getPaginatedStocks(page,size,filter));
    }
}
