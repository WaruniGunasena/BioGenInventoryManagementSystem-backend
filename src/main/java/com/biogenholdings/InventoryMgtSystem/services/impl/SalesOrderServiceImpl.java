package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.enums.UserRole;
import com.biogenholdings.InventoryMgtSystem.models.*;
import com.biogenholdings.InventoryMgtSystem.repositories.*;
import com.biogenholdings.InventoryMgtSystem.services.SalesOrderService;
import com.biogenholdings.InventoryMgtSystem.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional

public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceSequenceRepository invoiceSequenceRepository;
    private final UserService userService;

    @Override
    public String generateInvoiceNumber() {
        InvoiceSequence seq = invoiceSequenceRepository.findById(1L)
                .orElseGet(() -> {
                    InvoiceSequence newSeq = new InvoiceSequence();
                    newSeq.setId(1L);
                    newSeq.setLastNumber(0L);
                    return invoiceSequenceRepository.save(newSeq);
                });
        Long next = seq.getLastNumber() + 1;
        seq.setLastNumber(next);
        invoiceSequenceRepository.save(seq);

        return "BHG-" + Year.now().getValue() + "-" + String.format("%06d", next);
    }

    @Override
    public SalesOrderResponseDTO createSalesOrder(SalesOrderRequestDTO request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        String invoiceNumber = generateInvoiceNumber();

        User salesRep = new User();
        salesRep.setId(request.getUserId());
        SalesOrder order = SalesOrder.builder()
                .invoiceNumber(invoiceNumber)
                .customer(customer)
                .user(salesRep)
                .invoiceDate(request.getDate())
                .grandTotal(request.getGrandTotal())
                .build();

        List<SalesOrderItem> items = new ArrayList<>();

        for (SalesOrderItemRequestDTO itemReq : request.getItems()) {

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            ProductStock stock = productStockRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new RuntimeException("Product stock not found"));

            if (stock.getQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            stock.setQuantity(stock.getQuantity() - itemReq.getQuantity());
            productStockRepository.save(stock);

            SalesOrderItem item = SalesOrderItem.builder()
                    .salesOrder(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .sellingPrice(itemReq.getSellingPrice())
                    .totalAmount(itemReq.getTotalAmount())
                    .discountPercent(itemReq.getDiscountPercent())
                    .discountedPrice(itemReq.getDiscountedPrice())
                    .unit(itemReq.getUnit())
                    .build();

            items.add(item);
        }

        order.setItems(items);
        salesOrderRepository.save(order);

        // Build response
        List<SalesOrderItemResponseDTO> responseItems = items.stream()
                .map(i -> SalesOrderItemResponseDTO.builder()
                        .productId(i.getProduct().getId())
                        .productName(i.getProduct().getName())
                        .quantity(i.getQuantity())
                        .sellingPrice(i.getSellingPrice())
                        .totalAmount(i.getTotalAmount())
                        .discountPercent(i.getDiscountPercent())
                        .discountedPrice(i.getDiscountedPrice())
                        .unit(i.getUnit())
                        .build())
                .collect(Collectors.toList());

        return SalesOrderResponseDTO.builder()
                .invoiceNumber(invoiceNumber)
                .grandTotal(order.getGrandTotal())
                .items(responseItems)
                .build();
    }

    @Override
    public Response getPaginatedSalesOrders(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        User currentUser = userService.getCurrentLoggedInUser();

        Page<SalesOrder> salesPage;

        if (currentUser.getRole() == UserRole.ADMIN) {
            salesPage = salesOrderRepository.findAll(pageable);
        } else {
            salesPage = salesOrderRepository.findByUser_Id(currentUser.getId(), pageable);
        }

        List<SalesOrderResponseDTO> dtoList = salesPage
                .getContent()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.builder()
                .status(200)
                .message("Sales Orders fetched successfully")
                .salesOrderList(dtoList)
                .currentPage(salesPage.getNumber())
                .totalItems(salesPage.getTotalElements())
                .totalPages(salesPage.getTotalPages())
                .build();
    }

    private SalesOrderResponseDTO mapToDTO(SalesOrder order) {

        return SalesOrderResponseDTO.builder()
                .id(order.getId())
                .invoiceNumber(order.getInvoiceNumber())
                .invoiceDate(order.getInvoiceDate())
                .creditTerm(order.getCustomer().getCreditPeriod())
                .grandTotal(order.getGrandTotal())

                .customer(CustomerDTO.builder()
                        .id(order.getCustomer().getId())
                        .name(order.getCustomer().getName())
                        .contact_No(order.getCustomer().getContact_No())
                        .email(order.getCustomer().getEmail())
                        .address(order.getCustomer().getAddress())
                        .build())

                .user(UserDTO.builder()
                        .id(order.getUser().getId())
                        .name(order.getUser().getName())
                        .build())

                .items(order.getItems() == null ? List.of() :
                        order.getItems().stream()
                                .map(item -> SalesOrderItemResponseDTO.builder()
                                        .id(item.getId())
                                        .quantity(item.getQuantity())
                                        .sellingPrice(item.getSellingPrice())
                                        .totalAmount(item.getTotalAmount())
                                        .discountPercent(item.getDiscountPercent())
                                        .discountedPrice(item.getDiscountedPrice())
                                        .unit(item.getUnit())
                                        .product(ProductDTO.builder()
                                                .id(item.getProduct().getId())
                                                .name(item.getProduct().getName())
                                                .unit(item.getProduct().getUnit())
                                                .build())
                                        .build())
                                .toList())
                .build();
    }
}
