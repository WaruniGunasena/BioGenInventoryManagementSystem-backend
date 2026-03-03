package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.exceptions.NotFoundException;
import com.biogenholdings.InventoryMgtSystem.models.*;
import com.biogenholdings.InventoryMgtSystem.repositories.*;
import com.biogenholdings.InventoryMgtSystem.services.GRNService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GRNServiceImpl implements GRNService {

    private final GRNRepository grnRepository;
    private final GRNItemRepository grnItemRepository;
    private final ProductStockRepository productStockRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Response createGRN(GRNRequestDTO dto) {

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));

        GRN grn = GRN.builder()
                .grnNumber(generateGRNNumber())
                .invoiceNumber(dto.getInvoiceNumber())
                .grnDate(dto.getDate())
                .grandTotal(dto.getGrandTotal())
                .userId(dto.getUserId())
                .supplier(supplier)
                .build();

        grn = grnRepository.save(grn);

        for (GRNItemDTO itemDTO : dto.getItems()) {

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new NotFoundException(
                            "Product Not Found: " + itemDTO.getProductId()
                    ));

            /* ---------- Save GRN Item (History Layer) ---------- */
            GRNItem grnItem = GRNItem.builder()
                    .grn(grn)
                    .product(product)
                    .batchNumber(itemDTO.getBatchNumber())
                    .mfgDate(itemDTO.getMfgDate())
                    .expDate(itemDTO.getExpDate())
                    .purchasePrice(itemDTO.getPurchasePrice())
                    .quantity(itemDTO.getQuantity())
                    .totalAmount(itemDTO.getTotalAmount())
                    .build();

            grn.getItems().add(grnItem);
            grnItemRepository.save(grnItem);

            /* ---------- Stock Calculation (Live Layer) ---------- */

            BigDecimal purchasePrice = itemDTO.getPurchasePrice();

            // Pricing rule: 10% margin
            BigDecimal newSellingPrice = purchasePrice.multiply(BigDecimal.valueOf(1.10));

            ProductStock stock = productStockRepository
                    .findByProductId(product.getId())
                    .orElse(null);

            if (stock != null) {

                /* Quantity aggregation */
                int updatedQty = stock.getQuantity() + itemDTO.getQuantity();
                stock.setQuantity(updatedQty);

                /* Selling price rule: only increase */
                if (stock.getSellingPrice() == null ||
                        newSellingPrice.compareTo(stock.getSellingPrice()) > 0) {

                    stock.setSellingPrice(newSellingPrice);
                }

                productStockRepository.save(stock);

            } else {

                /* First stock record for product */
                ProductStock newStock = ProductStock.builder()
                        .product(product)
                        .quantity(itemDTO.getQuantity())
                        .sellingPrice(newSellingPrice)
                        .build();

                productStockRepository.save(newStock);
            }
        }

        GRNResponseDTO grnResponseDTO = mapToDTO(grn);

        return Response.builder()
                .status(201)
                .message("GRN added successfully and stock updated")
                .grn(grnResponseDTO)
                .build();
    }

    /* ---------------- Read Methods ---------------- */

    @Override
    public Response getAllGRNs() {
        List<GRNResponseDTO> grnDTOs = grnRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.builder()
                .status(200)
                .message("GRN list fetched successfully")
                .grnList(grnDTOs)
                .build();
    }

    @Override
    public Response getGRNById(Long id) {
        GRN grn = grnRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("GRN Not Found"));

        return Response.builder()
                .status(200)
                .message("Success")
                .grn(mapToDTO(grn))
                .build();
    }

    @Override
    public Response getGRNBySupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));

        List<GRNResponseDTO> grnDTOs = grnRepository.findBySupplier(supplier)
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.builder()
                .status(200)
                .message("Success")
                .grnList(grnDTOs)
                .build();
    }

    @Override
    public Response getPaginatedGRNs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<GRN> grnPage = grnRepository.findAll(pageable);

        List<GRNResponseDTO> grnDTOList = grnPage
                .getContent()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.builder()
                .status(200)
                .message("Paginated GRNs fetched successfully")
                .grnList(grnDTOList)
                .currentPage(grnPage.getNumber())
                .totalItems(grnPage.getTotalElements())
                .totalPages(grnPage.getTotalPages())
                .build();
    }

    /* ---------------- Mapping ---------------- */

    private GRNResponseDTO mapToDTO(GRN grn) {
        return GRNResponseDTO.builder()
                .id(grn.getId())
                .grnNumber(grn.getGrnNumber())
                .invoiceNumber(grn.getInvoiceNumber())
                .grnDate(grn.getGrnDate())
                .grandTotal(grn.getGrandTotal())
                .supplier(SupplierDTO.builder()
                        .id(grn.getSupplier().getId())
                        .name(grn.getSupplier().getName())
                        .phoneNumber(grn.getSupplier().getPhoneNumber())
                        .email(grn.getSupplier().getEmail())
                        .address(grn.getSupplier().getAddress())
                        .build())
                .items(grn.getItems() == null ? List.of() :
                        grn.getItems().stream()
                                .map(item -> GRNItemResponseDTO.builder()
                                        .id(item.getId())
                                        .product(ProductDTO.builder()
                                                .id(item.getProduct().getId())
                                                .name(item.getProduct().getName())
                                                .unit(item.getProduct().getUnit())
                                                .build())
                                        .batchNumber(item.getBatchNumber())
                                        .mfgDate(item.getMfgDate())
                                        .expDate(item.getExpDate())
                                        .purchasePrice(item.getPurchasePrice())
                                        .quantity(item.getQuantity())
                                        .totalAmount(item.getTotalAmount())
                                        .build())
                                .toList())
                .build();
    }

    private String generateGRNNumber() {
        return "GRN-" + System.currentTimeMillis();
    }
}