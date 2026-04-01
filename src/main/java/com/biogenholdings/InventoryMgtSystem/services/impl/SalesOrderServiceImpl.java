package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.enums.DiscountTypeEnum;
import com.biogenholdings.InventoryMgtSystem.enums.SalesOrderStatus;
import com.biogenholdings.InventoryMgtSystem.enums.UserRole;
import com.biogenholdings.InventoryMgtSystem.exceptions.NotFoundException;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional

public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceSequenceRepository invoiceSequenceRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SalesOrderPaymentRepository salesOrderPaymentRepository;

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

        User userRole = userRepository.findById(request.getUserId())
                .orElseThrow(()-> new RuntimeException("Sales Rep not found"));
        SalesOrder order = SalesOrder.builder()
                .invoiceNumber(invoiceNumber)
                .customer(customer)
                .user(salesRep)
                .invoiceDate(request.getDate())
                .grandTotal(request.getGrandTotal())
                .isDeleted(false)
                .status(SalesOrderStatus.Pending)
                .creditTerm(customer.getCreditPeriod())
                .additionalDiscount(request.getAdditionalDiscountValue())
                .courierCharges(request.getCourierCharges())
                .additionalDiscountType(request.getAdditionalDiscountType())
                .build();

        List<SalesOrderItem> items = new ArrayList<>();

        for (SalesOrderItemRequestDTO itemReq : request.getItems()) {

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            ProductStock stock = productStockRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new RuntimeException("Product stock not found"));

            if (stock.getTotalQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            stock.setTotalQuantity(stock.getTotalQuantity() - itemReq.getQuantity());
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

    @Override
    public Response softDeleteSalesOrder(Long salesOrderId, Long userId) {
        SalesOrder salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new NotFoundException("Sales Order Not Found"));
        if (salesOrder.getStatus() == SalesOrderStatus.Approved) {
            throw new RuntimeException("Cannot delete approved orders");
        }

        if (Boolean.TRUE.equals(salesOrder.getIsDeleted())) {
            throw new RuntimeException("Sales Order already deleted");
        }

        for (SalesOrderItem item : salesOrder.getItems()) {

            ProductStock stock = productStockRepository.findByProductId(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Stock not found for product: " + item.getProduct().getName()));

            stock.setTotalQuantity(
                    stock.getTotalQuantity() + item.getQuantity()
            );

            productStockRepository.save(stock);
        }

        User user = new User();
        user.setId(userId);

        salesOrder.setIsDeleted(true);
        salesOrder.setDeletedBy(user);
        salesOrder.setDeletedAt(LocalDateTime.now());

        salesOrder.setStatus(SalesOrderStatus.Deleted);

        salesOrderRepository.save(salesOrder);

        return Response.builder()
                .status(200)
                .message("Sales Order deleted successfully and stock restored")
                .build();


    }

    @Override
    @Transactional
    public SalesOrderResponseDTO updateSalesOrder(Long orderId, SalesOrderRequestDTO request, Long userId) {

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sales Order not found"));

        if (Boolean.TRUE.equals(order.getIsDeleted())) {
            throw new RuntimeException("Cannot edit deleted order");
        }

        if (order.getStatus() != SalesOrderStatus.Pending) {
            throw new RuntimeException("Only PENDING orders can be edited");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (SalesOrderItem oldItem : order.getItems()) {

            ProductStock stock = productStockRepository
                    .findByProductId(oldItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Stock not found"));

            stock.setTotalQuantity(
                    stock.getTotalQuantity() + oldItem.getQuantity()
            );

            productStockRepository.save(stock);
        }

        order.getItems().clear();

        for (SalesOrderItemRequestDTO itemReq : request.getItems()) {

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            ProductStock stock = productStockRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new RuntimeException("Stock not found"));

            if (stock.getTotalQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            stock.setTotalQuantity(
                    stock.getTotalQuantity() - itemReq.getQuantity()
            );

            productStockRepository.save(stock);

            SalesOrderItem newItem = SalesOrderItem.builder()
                    .salesOrder(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .sellingPrice(itemReq.getSellingPrice())
                    .totalAmount(itemReq.getTotalAmount())
                    .discountPercent(itemReq.getDiscountPercent())
                    .discountedPrice(itemReq.getDiscountedPrice())
                    .unit(itemReq.getUnit())
                    .build();

            order.getItems().add(newItem);
        }

        order.setAdditionalDiscount(request.getAdditionalDiscountValue());
        order.setAdditionalDiscountType(request.getAdditionalDiscountType());
        order.setCourierCharges(request.getCourierCharges());
        order.setGrandTotal(request.getGrandTotal());

        salesOrderRepository.save(order);

        return mapToDTO(order);
    }

    @Override
    public Response approveSalesOrder(SalesOrderStatus salesOrderStatus, Long userId, Long salesOrderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        if(user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.INVENTORY_MANAGER){
            throw new RuntimeException("Unauthorized to approve");
        }

        SalesOrder salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new NotFoundException("Order Not Found"));

        if (salesOrderStatus == SalesOrderStatus.Rejected) {

            if (salesOrder.getStatus() == SalesOrderStatus.Approved || salesOrder.getStatus() == SalesOrderStatus.Pending) {
                for (SalesOrderItem item : salesOrder.getItems()) {
                    ProductStock stock = productStockRepository.findByProductId(item.getProduct().getId())
                            .orElseThrow(() -> new RuntimeException("Stock not found for product: " + item.getProduct().getName()));

                    stock.setTotalQuantity(stock.getTotalQuantity() + item.getQuantity());
                    productStockRepository.save(stock);
                }
            }
        }

        salesOrder.setStatus(salesOrderStatus);
        salesOrder.setApprovedBy(user);
        salesOrder.setApprovedAt(LocalDateTime.now());

        String message = (salesOrderStatus == SalesOrderStatus.Rejected)
                ? "Sales order has been rejected and stock has been restored."
                : "Sales order approved successfully.";

        return Response.builder()
                .status(200)
                .message(message)
                .build();
    }

    @Override
    public Long pendingSalesOrderCount() {

        return salesOrderRepository.countByStatusAndIsDeletedFalse(SalesOrderStatus.Pending);
    }

    @Override
    public Response createSalesOrderPayment(SalesOrderPaymentDTO dto) {
        try {
            SalesOrder order = salesOrderRepository.findById(dto.getSalesOrderId())
                    .orElseThrow(() -> new NotFoundException("Sales Order not found with ID: " + dto.getSalesOrderId()));

            SalesOrderPayment payment = new SalesOrderPayment();
            payment.setAmount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO);
            payment.setGrandTotal(dto.getGrandTotal() != null ? dto.getGrandTotal() : BigDecimal.ZERO); // <-- MUST set this
            payment.setPaymentMethod(dto.getPaymentMethod());
            payment.setSalesOrder(order);
            payment.setCreatedBy(dto.getUserId());
            payment.setCreatedAt(LocalDateTime.now());

            String paymentMethod = dto.getPaymentMethod() != null ? dto.getPaymentMethod().trim() : "";

            if (!"cash".equalsIgnoreCase(paymentMethod)) {
                payment.setBank(dto.getBank() != null ? dto.getBank().trim() : null);
                payment.setChequeNumber(dto.getChequeNumber() != null ? dto.getChequeNumber().trim() : null);

                if (dto.getChequeIssueDate() != null && !dto.getChequeIssueDate().isBlank()) {
                    payment.setChequeIssueDate(LocalDate.parse(dto.getChequeIssueDate().trim()));
                }

                if (dto.getChequeDueDate() != null && !dto.getChequeDueDate().isBlank()) {
                    payment.setChequeDueDate(LocalDate.parse(dto.getChequeDueDate().trim()));
                }
            }

            salesOrderPaymentRepository.save(payment);

            BigDecimal totalPaid = salesOrderPaymentRepository
                    .findBySalesOrderId(dto.getSalesOrderId())
                    .stream()
                    .map(SalesOrderPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal dueBalance = order.getGrandTotal().subtract(totalPaid);
            dueBalance = dueBalance.max(BigDecimal.ZERO);
            payment.setDueBalance(dueBalance);

            if (dueBalance.compareTo(BigDecimal.ZERO) == 0) {
                order.setPaymentStatus("PAID");
            } else if (dueBalance.compareTo(order.getGrandTotal()) < 0) {
                order.setPaymentStatus("PARTIAL");
            } else {
                order.setPaymentStatus("UNPAID");
            }

            salesOrderPaymentRepository.save(payment);
            salesOrderRepository.save(order);


            return Response.builder()
                    .status(200)
                    .message("Payment recorded successfully")
                    .build();

        } catch (Exception e) {
            return Response.builder()
                    .status(500)
                    .message("Failed to create SalesOrder payment: " + e.getMessage())
                    .build();
        }
    }


    private SalesOrderResponseDTO mapToDTO(SalesOrder order) {

        SalesOrderPayment latestPayment = salesOrderPaymentRepository
                .findTopBySalesOrderIdOrderByIdDesc(order.getId());

        BigDecimal dueBalance;
        BigDecimal totalPaid;

        if (latestPayment != null) {
            dueBalance = latestPayment.getDueBalance();
            totalPaid = order.getGrandTotal().subtract(dueBalance);
        } else {
            dueBalance = order.getGrandTotal();
            totalPaid = BigDecimal.ZERO;
        }

        return SalesOrderResponseDTO.builder()
                .id(order.getId())
                .invoiceNumber(order.getInvoiceNumber())
                .invoiceDate(order.getInvoiceDate())
                .creditTerm(order.getCustomer().getCreditPeriod())
                .grandTotal(order.getGrandTotal())
                .courierCharges(order.getCourierCharges())
                .additionalDiscountValue(order.getAdditionalDiscount())
                .additionalDiscountType(order.getAdditionalDiscountType())
                .status(order.getStatus())
                .totalPaid(totalPaid)
                .dueBalance(dueBalance)
                .paymentStatus(order.getPaymentStatus())
                .netTotal(calculateNetTotal(order.getGrandTotal(),order.getCourierCharges(),order.getAdditionalDiscount(),order.getAdditionalDiscountType()))

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
                        .role(order.getUser().getRole())
                        .build())

                .items(order.getItems() == null ? List.of() :
                        order.getItems().stream()
                                .map(item -> SalesOrderItemResponseDTO.builder()
                                        .id(item.getId())
                                        .quantity(item.getQuantity())
                                        .sellingPrice(item.getSellingPrice())
                                        .totalAmount(item.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
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

    private BigDecimal calculateNetTotal(
            BigDecimal grandTotal,
            BigDecimal courierCharges,
            BigDecimal discountValue,
            DiscountTypeEnum discountType
    ) {
        if (grandTotal == null) grandTotal = BigDecimal.ZERO;
        if (courierCharges == null) courierCharges = BigDecimal.ZERO;
        if (discountValue == null) discountValue = BigDecimal.ZERO;

        BigDecimal discountAmount = BigDecimal.ZERO;

        if (discountType == DiscountTypeEnum.cash) {
            discountAmount = discountValue;
        } else if (discountType == DiscountTypeEnum.percentage) {
            discountAmount = grandTotal
                    .multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        return grandTotal
                .add(courierCharges)
                .subtract(discountAmount);
    }

}
