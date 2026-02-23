package com.biogenholdings.InventoryMgtSystem.controllers;

import com.biogenholdings.InventoryMgtSystem.dtos.ProductDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.enums.FilterEnum;
import com.biogenholdings.InventoryMgtSystem.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")

public class ProductController {

    private final ProductService productService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<Response> saveProduct(
            @RequestParam(value = "imageFile", required = false)MultipartFile imageFile,
            @RequestParam("name")String name,
            @RequestParam("minimumStockLevel")Integer minimumStockLevel,
            @RequestParam("unit")String unit,
            @RequestParam("reorderLevel")Integer reorderLevel,
            @RequestParam("categoryId")Long categoryId,
            @RequestParam(value = "description", required = false)String description
            ){
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(name);
        productDTO.setUnit(unit);
        productDTO.setMinimumStockLevel(minimumStockLevel);
        productDTO.setReorderLevel(reorderLevel);
        productDTO.setCategoryId(categoryId);
        productDTO.setDescription(description);

        return ResponseEntity.ok(productService.saveProduct(productDTO, imageFile));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<Response> updateProduct(
            @PathVariable Long id,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "sellingPrice", required = false) BigDecimal sellingPrice,
            @RequestParam(value = "minimumStockLevel", required = false) Integer minimumStockLevel,
            @RequestParam(value = "reorderLevel", required = false) Integer reorderLevel,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "description", required = false) String description
    ) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(id);
        productDTO.setName(name);
        productDTO.setMinimumStockLevel(minimumStockLevel);
        productDTO.setReorderLevel(reorderLevel);
        productDTO.setCategoryId(categoryId);
        productDTO.setDescription(description);

        return ResponseEntity.ok(productService.updateProduct(productDTO, imageFile));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllProducts(){
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getProductById(@PathVariable Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<Response> deleteProduct(@PathVariable Long id){
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Response> searchProduct(@RequestParam String searchKey){
        return ResponseEntity.ok(productService.searchProduct(searchKey));
    }

    @GetMapping
    public ResponseEntity<Response> paginatedResults(@RequestParam(defaultValue = "0") Integer page,
                                                     @RequestParam(defaultValue = "5") Integer size,
                                                     @RequestParam(defaultValue = "ASC") FilterEnum filter){
        return ResponseEntity.ok(productService.getPaginatedProducts(page,size,filter));
    }

}
