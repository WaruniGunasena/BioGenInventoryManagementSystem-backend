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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
                .updatedBy(dto.getUserId())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .deletedBy(null)
                .deletedAt(null)
                .build();

        grn = grnRepository.save(grn);

        for (GRNItemDTO itemDTO : dto.getItems()) {

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new NotFoundException(
                            "Product Not Found: " + itemDTO.getProductId()
                    ));

            int totalReceived = itemDTO.getQuantity() + itemDTO.getBonus();

            GRNItem grnItem = GRNItem.builder()
                    .grn(grn)
                    .product(product)
                    .batchNumber(itemDTO.getBatchNumber())
                    .mfgDate(itemDTO.getMfgDate())
                    .expDate(itemDTO.getExpDate())
                    .purchasePrice(itemDTO.getPurchasePrice())
                    .quantity(itemDTO.getQuantity())
                    .bonus(itemDTO.getBonus())
                    .packSize(itemDTO.getPackSize())
                    .mrpValue(itemDTO.getMrpValue())
                    .totalAmount(itemDTO.getTotalAmount())
                    .SellingPricePercentage(itemDTO.getSellingPricePercentage())
                    .discountValue(itemDTO.getDiscountValue())
                    .discountPercentage(itemDTO.getDiscountPercentage())
                    .createdAt(LocalDateTime.now())
                    .isDeleted(false)
                    .deletedBy(null)
                    .deletedAt(null)
                    .build();

            grn.getItems().add(grnItem);
            grnItemRepository.save(grnItem);

            product.setMrp(itemDTO.getMrpValue());
            productRepository.save(product);

            BigDecimal purchasePrice = itemDTO.getPurchasePrice();
            BigDecimal percentage = itemDTO.getSellingPricePercentage();

            BigDecimal margin = purchasePrice
                    .multiply(percentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            BigDecimal newSellingPrice = purchasePrice.add(margin);

            ProductStock stock = productStockRepository
                    .findByProductId(product.getId())
                    .orElse(null);

            if (stock != null) {

                int updatedQty = stock.getTotalQuantity() + totalReceived;
                stock.setTotalQuantity(updatedQty);

                if (stock.getSellingPrice() == null ||
                        newSellingPrice.compareTo(stock.getSellingPrice()) > 0) {

                    stock.setSellingPrice(newSellingPrice);
                }

                productStockRepository.save(stock);

            } else {

                ProductStock newStock = ProductStock.builder()
                        .product(product)
                        .totalQuantity(totalReceived)
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

        Page<GRN> grnPage = grnRepository.findByIsDeletedFalse(pageable);

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

    @Override
    @Transactional
    public Response updateGRN(Long grnId, GRNRequestDTO dto) {

        GRN grn = grnRepository.findById(grnId)
                .orElseThrow(() -> new NotFoundException("GRN Not Found"));

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));

        grn.setInvoiceNumber(dto.getInvoiceNumber());
        grn.setGrnDate(dto.getDate());
        grn.setGrandTotal(dto.getGrandTotal());
        grn.setUpdatedAt(LocalDateTime.now());
        grn.setUpdatedBy(dto.getUserId());
        grn.setSupplier(supplier);

        List<GRNItem> existingItems = grnItemRepository.findByGrnId(grnId);

        List<Long> incomingIds = dto.getItems().stream()
                .map(GRNItemDTO::getId)
                .filter(Objects::nonNull)
                .toList();

        for (GRNItem existingItem : existingItems) {

            if (existingItem.getId() != null && !incomingIds.contains(existingItem.getId())) {

                int oldQty = existingItem.getQuantity() + existingItem.getBonus();

                ProductStock stock = productStockRepository
                        .findByProductId(existingItem.getProduct().getId())
                        .orElse(null);

                if (stock != null) {
                    int updatedQty = stock.getTotalQuantity() - oldQty;
                    if (updatedQty < 0) updatedQty = 0;

                    stock.setTotalQuantity(updatedQty);
                    productStockRepository.save(stock);
                }

                existingItem.setIsDeleted(true);
                existingItem.setDeletedBy(dto.getUserId());
                existingItem.setDeletedAt(LocalDateTime.now());

                grnItemRepository.save(existingItem);
            }
        }

        for (GRNItemDTO itemDTO : dto.getItems()) {

            GRNItem grnItem;
            Product product;

            if (itemDTO.getId() != null) {

                grnItem = grnItemRepository.findById(itemDTO.getId())
                        .orElseThrow(() -> new NotFoundException(
                                "GRN Item Not Found: " + itemDTO.getId()));

                int oldQty = grnItem.getQuantity() + grnItem.getBonus();

                ProductStock stock = productStockRepository
                        .findByProductId(grnItem.getProduct().getId())
                        .orElse(null);

                if (stock != null) {
                    stock.setTotalQuantity(stock.getTotalQuantity() - oldQty);
                    productStockRepository.save(stock);
                }

                product = grnItem.getProduct();

            }

            else {

                product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new NotFoundException(
                                "Product Not Found: " + itemDTO.getProductId()));

                grnItem = GRNItem.builder()
                        .grn(grn)
                        .product(product)
                        .createdAt(LocalDateTime.now())
                        .isDeleted(false)
                        .build();
            }

            int totalReceived = itemDTO.getQuantity() + itemDTO.getBonus();

            grnItem.setBatchNumber(itemDTO.getBatchNumber());
            grnItem.setMfgDate(itemDTO.getMfgDate());
            grnItem.setExpDate(itemDTO.getExpDate());
            grnItem.setPurchasePrice(itemDTO.getPurchasePrice());
            grnItem.setQuantity(itemDTO.getQuantity());
            grnItem.setBonus(itemDTO.getBonus());
            grnItem.setPackSize(itemDTO.getPackSize());
            grnItem.setTotalAmount(itemDTO.getTotalAmount());
            grnItem.setDiscountValue(itemDTO.getDiscountValue());
            grnItem.setMrpValue(itemDTO.getMrpValue());
            grnItem.setDiscountPercentage(itemDTO.getDiscountPercentage());
            grnItem.setSellingPricePercentage(itemDTO.getSellingPricePercentage());
            grnItem.setUpdatedAt(LocalDateTime.now());
            grnItem.setUpdatedBy(dto.getUserId());

            grnItemRepository.save(grnItem);

            BigDecimal purchasePrice = itemDTO.getPurchasePrice();
            BigDecimal percentage = itemDTO.getSellingPricePercentage();

            BigDecimal margin = purchasePrice
                    .multiply(percentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            BigDecimal newSellingPrice = purchasePrice.add(margin);

            ProductStock stock = productStockRepository
                    .findByProductId(product.getId())
                    .orElse(null);

            if (stock != null) {

                int updatedQty = stock.getTotalQuantity() + totalReceived;
                stock.setTotalQuantity(updatedQty);

                if (stock.getSellingPrice() == null ||
                        newSellingPrice.compareTo(stock.getSellingPrice()) > 0) {

                    stock.setSellingPrice(newSellingPrice);
                }

                productStockRepository.save(stock);

            } else {

                ProductStock newStock = ProductStock.builder()
                        .product(product)
                        .totalQuantity(totalReceived)
                        .sellingPrice(newSellingPrice)
                        .build();

                productStockRepository.save(newStock);
            }
        }

        grnRepository.save(grn);

        return Response.builder()
                .status(200)
                .message("GRN updated successfully")
                .grn(mapToDTO(grn))
                .build();
    }

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
                        .creditPeriod(grn.getSupplier().getCreditPeriod())
                        .build())
                .items(grn.getItems() == null ? List.of() :
                        grn.getItems().stream()
                                .map(item -> GRNItemResponseDTO.builder()
                                        .id(item.getId())
                                        .product(ProductDTO.builder()
                                                .id(item.getProduct().getId())
                                                .name(item.getProduct().getName())
                                                .unit(item.getProduct().getUnit())
                                                .itemCode(item.getProduct().getItemCode())
                                                .build())
                                        .batchNumber(item.getBatchNumber())
                                        .mfgDate(item.getMfgDate())
                                        .expDate(item.getExpDate())
                                        .purchasePrice(item.getPurchasePrice())
                                        .quantity(item.getQuantity())
                                        .bonus(item.getBonus())
                                        .packSize(item.getPackSize())
                                        .totalAmount(item.getTotalAmount())
                                        .discountValue(item.getDiscountValue())
                                        .discountPercentage(item.getDiscountPercentage())
                                        .mrpValue(item.getMrpValue())
                                        .SellingPricePercentage(item.getSellingPricePercentage())
                                        .build())
                                .toList())
                .build();
    }

    private String generateGRNNumber() {
        return "GRN-" + System.currentTimeMillis();
    }

    @Override
    @Transactional
    public Response softDeleteGRN(Long grnId, Long userId) {

        GRN grn = grnRepository.findById(grnId)
                .orElseThrow(() -> new NotFoundException("GRN not found"));

        List<GRNItem> items = grnItemRepository.findByGrnId(grnId);

        for (GRNItem item : items) {

            int totalReceived = item.getQuantity() + item.getBonus();

            ProductStock stock = productStockRepository
                    .findByProductId(item.getProduct().getId())
                    .orElse(null);

            if (stock != null) {

                int updatedQty = stock.getTotalQuantity() - totalReceived;

                if (updatedQty < 0) {
                    updatedQty = 0;
                }

                stock.setTotalQuantity(updatedQty);
                productStockRepository.save(stock);
            }

            item.setIsDeleted(true);
            item.setDeletedBy(userId);
            item.setDeletedAt(LocalDateTime.now());

            grnItemRepository.save(item);
        }

        grn.setIsDeleted(true);
        grn.setDeletedBy(userId);
        grn.setDeletedAt(LocalDateTime.now());

        grnRepository.save(grn);

        return Response.builder()
                .status(204)
                .message("Category Deleted Successfully")
                .build();
    }
}