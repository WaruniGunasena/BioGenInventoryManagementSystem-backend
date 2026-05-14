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
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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
import java.time.temporal.ChronoUnit;
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
    private final ProductReturnItemRepository productReturnItemRepository;
    private final ModelMapper modelMapper;
    private final SalesCommissionSummaryRepository commissionSummaryRepository;

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

        LocalDate invoiceDate = request.getDate();
        LocalDate overDueOn = null;

        String creditPeriodStr = customer.getCreditPeriod();

        // Check if credit period exists and is not "CASH" (case-insensitive)
        if (creditPeriodStr != null && !"CASH".equalsIgnoreCase(creditPeriodStr.trim())) {
            try {
                // Extract numbers only (handles "30 days" or just "30")
                int days = Integer.parseInt(creditPeriodStr.replaceAll("[^0-9]", ""));
                overDueOn = invoiceDate.plusDays(days);
            } catch (NumberFormatException e) {
                // If parsing fails, treat as cash/null or log warning
                throw new RuntimeException("Error",e);
            }
        }
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
                .returnCredits(request.getReturnCredits())
                .isDelivered(false)
                .overDueOn(overDueOn)
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

            BigDecimal itemTotalAmount;
            Integer itemReturnQty;

            if (Boolean.TRUE.equals(itemReq.getIsReissue())) {

                itemTotalAmount = BigDecimal.ZERO;
                itemReturnQty = itemReq.getQuantity();

                updatePendingReturnItems(customer.getId(), product.getId(), itemReq.getQuantity());
            } else {
                itemTotalAmount = itemReq.getTotalAmount();
                itemReturnQty = 0;
            }

            SalesOrderItem item = SalesOrderItem.builder()
                    .salesOrder(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .sellingPrice(itemReq.getSellingPrice())
                    .totalAmount(itemTotalAmount)
                    .discountPercent(itemReq.getDiscountPercent())
                    .discountedPrice(itemReq.getDiscountedPrice())
                    .unit(itemReq.getUnit())
                    .isReissue(itemReq.getIsReissue())
                    .returnQty(itemReturnQty)
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

        customer.setDueBalance(customer.getDueBalance().add(calculateNetTotal(order.getGrandTotal(),order.getCourierCharges(),order.getAdditionalDiscount(),BigDecimal.ZERO,order.getAdditionalDiscountType())));

        if (request.getReturnCredits().compareTo(BigDecimal.ZERO) > 0) {
            customer.setAvailableReturnCredit(customer.getAvailableReturnCredit().subtract(request.getReturnCredits()));
        }

        customerRepository.save(customer);
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
            salesPage = salesOrderRepository.findByIsDeleted(false,pageable);
        } else {
            salesPage = salesOrderRepository.findByUser_IdAndIsDeletedFalse(currentUser.getId(), pageable);
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

        Customer customer = customerRepository.findById(salesOrder.getCustomer().getId())
                .orElseThrow(() -> new NotFoundException("customer not found"));
        if (salesOrder.getStatus() == SalesOrderStatus.Approved) {
            throw new RuntimeException("Cannot delete approved orders");
        }

        if (Boolean.TRUE.equals(salesOrder.getIsDeleted())) {
            throw new RuntimeException("Sales Order already deleted");
        }

        for (SalesOrderItem item : salesOrder.getItems()) {

            ProductStock stock = productStockRepository.findByProductId(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Stock not found for product: " + item.getProduct().getName()));
            if(salesOrder.getStatus() != SalesOrderStatus.Rejected) {
                stock.setTotalQuantity(
                        stock.getTotalQuantity() + item.getQuantity()
                );
                customer.setDueBalance(customer.getDueBalance().subtract(calculateNetTotal(salesOrder.getGrandTotal(), salesOrder.getCourierCharges(), salesOrder.getAdditionalDiscount(), salesOrder.getReturnCredits(), salesOrder.getAdditionalDiscountType())));
                if (salesOrder.getReturnCredits().compareTo(BigDecimal.ZERO) > 0) {
                    customer.setAvailableReturnCredit(customer.getAvailableReturnCredit().add(salesOrder.getReturnCredits()));
                }

                // 4. Reverse Reissue logic
                reverseReissueLogic(salesOrder);

                customerRepository.save(customer);
            }

            productStockRepository.save(stock);
        }

        User user = new User();
        user.setId(userId);

        salesOrder.setIsDeleted(true);
        salesOrder.setDeletedBy(user);
        salesOrder.setDeletedAt(LocalDateTime.now());
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

        Customer customer = customerRepository.findById(order.getCustomer().getId())
                .orElseThrow(()-> new NotFoundException("Customer not Found"));

        BigDecimal oldNet = calculateNetTotal(
                order.getGrandTotal(),
                order.getCourierCharges(),
                order.getAdditionalDiscount(),
                order.getReturnCredits(),
                order.getAdditionalDiscountType()
        );
        customer.setDueBalance(customer.getDueBalance().subtract(oldNet));
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
        order.setReturnCredits(request.getReturnCredits());

        BigDecimal newNet = calculateNetTotal(
                request.getGrandTotal(),
                request.getCourierCharges(),
                request.getAdditionalDiscountValue(),
                request.getReturnCredits(),
                request.getAdditionalDiscountType()
        );
        customer.setDueBalance(customer.getDueBalance().add(newNet));

        // Save both to persist the changes
        customerRepository.save(customer);

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
                Customer customer = customerRepository.findById(salesOrder.getCustomer().getId())
                        .orElseThrow(() -> new NotFoundException("Customer not found"));

                customer.setDueBalance(customer.getDueBalance().subtract(calculateNetTotal(salesOrder.getGrandTotal(),salesOrder.getCourierCharges(),salesOrder.getAdditionalDiscount(),salesOrder.getReturnCredits(),salesOrder.getAdditionalDiscountType())));

                // 3. Restore Return Credits
                if (salesOrder.getReturnCredits().compareTo(BigDecimal.ZERO) > 0) {
                    customer.setAvailableReturnCredit(customer.getAvailableReturnCredit().add(salesOrder.getReturnCredits()));
                }

                // 4. Reverse Reissue
                reverseReissueLogic(salesOrder);
                customerRepository.save(customer);
            }
        }

        if (salesOrderStatus == SalesOrderStatus.Approved) {
            BigDecimal netTotal = calculateNetTotal(
                    salesOrder.getGrandTotal(),
                    salesOrder.getCourierCharges(),
                    salesOrder.getAdditionalDiscount(),
                    salesOrder.getReturnCredits(),
                    salesOrder.getAdditionalDiscountType()
            );

            if (netTotal.compareTo(BigDecimal.ZERO) == 0) {
                salesOrder.setPaymentStatus("PAID"); // fully covered by credits
            } else {
                salesOrder.setPaymentStatus("PENDING");
            }

            if (salesOrder.getUser() != null && salesOrder.getUser().getRole() == UserRole.SALES_REP) {
                createCommissionRecords(salesOrder);
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

    private void createCommissionRecords(SalesOrder order) {

        SalesCommissionSummary summary = SalesCommissionSummary.builder()
                .salesOrderId(order.getId().toString())
                .invoiceNumber(order.getInvoiceNumber())
                .salesRepId(order.getUser().getId())
                .customer(order.getCustomer())
                .invoiceDate(order.getInvoiceDate().atStartOfDay())
                .CommissionableAmount(BigDecimal.ZERO)
                .ReturnCommission(BigDecimal.ZERO)
                .TotalCommission(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalOrderCommission = BigDecimal.ZERO;
        BigDecimal totalCommissionableAmount = BigDecimal.ZERO;

        List<SalesCommissionItem> commissionItems = new ArrayList<>();

        for (SalesOrderItem orderItem : order.getItems()) {
            Product product = orderItem.getProduct();

            BigDecimal ratePercent = (product.getSRepCommissionRate() != null) ?
                    product.getSRepCommissionRate() : BigDecimal.ZERO;

            if (ratePercent.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal itemTotalValue = orderItem.getSellingPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            BigDecimal itemCommission = itemTotalValue.multiply(ratePercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

            SalesCommissionItem commItem = SalesCommissionItem.builder()
                    .summary(summary)
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(orderItem.getQuantity())
                    .sellingPrice(orderItem.getSellingPrice())
                    .commissionRate(ratePercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                    .earnedCommission(itemCommission)
                    .isReturned(false)
                    .build();

            commissionItems.add(commItem);

            totalCommissionableAmount = totalCommissionableAmount.add(itemTotalValue);
            totalOrderCommission = totalOrderCommission.add(itemCommission);
        }

        if (!commissionItems.isEmpty()) {
            summary.setItems(commissionItems);
            summary.setCommissionableAmount(totalCommissionableAmount);
            summary.setTotalCommission(totalOrderCommission);

            commissionSummaryRepository.save(summary);
        }
    }

    @Override
    public Long pendingSalesOrderCount() {
        return salesOrderRepository.countByStatusAndIsDeletedFalse(SalesOrderStatus.Pending);
    }

    @Override
    @Transactional
    public Response createSalesOrderPayment(SalesOrderPaymentDTO dto) {
        try {
            // 1. Fetch the Sales Order
            SalesOrder order = salesOrderRepository.findById(dto.getSalesOrderId())
                    .orElseThrow(() -> new NotFoundException("Sales Order not found with ID: " + dto.getSalesOrderId()));

            // 2. Map DTO to Entity
            SalesOrderPayment payment = new SalesOrderPayment();
            payment.setAmount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO);
            payment.setGrandTotal(dto.getGrandTotal() != null ? dto.getGrandTotal() : BigDecimal.ZERO);
            payment.setPaymentMethod(dto.getPaymentMethod());
            payment.setSalesOrder(order);
            payment.setCreatedBy(dto.getUserId());
            payment.setCreatedAt(LocalDateTime.now());

            String paymentMethod = dto.getPaymentMethod() != null ? dto.getPaymentMethod().trim() : "";
            boolean isCheque = "cheque".equalsIgnoreCase(paymentMethod);

            // 3. Handle Payment Lifecycle Status
            if (isCheque) {
                payment.setStatus("REALIZING"); // The "Pending" state
                payment.setBank(dto.getBank() != null ? dto.getBank().trim() : null);
                payment.setChequeNumber(dto.getChequeNumber() != null ? dto.getChequeNumber().trim() : null);

                if (dto.getChequeIssueDate() != null && !dto.getChequeIssueDate().isBlank()) {
                    payment.setChequeIssueDate(LocalDate.parse(dto.getChequeIssueDate().trim()));
                }
                if (dto.getChequeDueDate() != null && !dto.getChequeDueDate().isBlank()) {
                    payment.setChequeDueDate(LocalDate.parse(dto.getChequeDueDate().trim()));
                }
            } else {
                payment.setStatus("PAID"); // Cash is realized immediately
            }

            // 4. Calculate Sales Order Due Balance
            // Logic: Include existing Cash (PAID) and existing Realized Cheques (REALIZED)
            BigDecimal existingConfirmedPaid = salesOrderPaymentRepository
                    .findBySalesOrderId(dto.getSalesOrderId())
                    .stream()
                    .filter(p -> "PAID".equalsIgnoreCase(p.getStatus()) || "REALIZED".equalsIgnoreCase(p.getStatus()))
                    .map(SalesOrderPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // To avoid overlap, we also check if there are any other 'REALIZING' (pending) cheques
            // already in the system and treat them as "paid" for the sake of the balance calculation.
            BigDecimal pendingChequesTotal = salesOrderPaymentRepository
                    .findBySalesOrderId(dto.getSalesOrderId())
                    .stream()
                    .filter(p -> "REALIZING".equalsIgnoreCase(p.getStatus()))
                    .map(SalesOrderPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Total deduction = Previous Confirmed + Previous Pending + Current Payment (Cash or Cheque)
            BigDecimal totalDeduction = existingConfirmedPaid
                    .add(pendingChequesTotal)
                    .add(payment.getAmount());

            BigDecimal netTotal = calculateNetTotal(order.getGrandTotal(), order.getCourierCharges(),
                    order.getAdditionalDiscount(), order.getReturnCredits(),
                    order.getAdditionalDiscountType());

            // Now dueBalance will be (Total - All payments submitted to date)
            BigDecimal dueBalance = netTotal.subtract(totalDeduction).max(BigDecimal.ZERO);
            payment.setDueBalance(dueBalance);

            // 5. Update Sales Order Table Payment Status
            if (isCheque) {
                // Setting this to REALIZING triggers your "Realize" button on the frontend
                order.setPaymentStatus("REALIZING");
            } else {
                if (dueBalance.compareTo(BigDecimal.ZERO) == 0) {
                    order.setPaymentStatus("PAID");
                    order.setOverDueOn(null);
                } else if (dueBalance.compareTo(netTotal) < 0) {
                    order.setPaymentStatus("PARTIAL");
                } else {
                    order.setPaymentStatus("UNPAID");
                }
            }

            // 6. Update Customer Table Due Balance
            // Requirement: Reduce customer's total debt immediately upon cheque receipt
            Customer customer = customerRepository.findById(order.getCustomer().getId())
                    .orElseThrow(() -> new NotFoundException("Customer not found"));

            customer.setDueBalance(customer.getDueBalance().subtract(dto.getAmount()));

            // 7. Persist All Changes
            customerRepository.save(customer);
            salesOrderPaymentRepository.save(payment);
            salesOrderRepository.save(order);

            return Response.builder()
                    .status(200)
                    .message(isCheque ? "Cheque recorded. Status set to REALIZING." : "Payment successful.")
                    .build();

        } catch (Exception e) {
            return Response.builder()
                    .status(500)
                    .message("Failed to record payment: " + e.getMessage())
                    .build();
        }
    }

    public List<SalesOrderPayment> getPendingChequesByOrderId(Long salesOrderId) {
        return salesOrderPaymentRepository.findBySalesOrderId(salesOrderId)
                .stream()
                .filter(p -> "REALIZING".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Response processChequeStatus(Long paymentId, String status) {
        SalesOrderPayment payment = salesOrderPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment record not found"));

        SalesOrder order = payment.getSalesOrder();

        try {
            if ("PAID".equalsIgnoreCase(status)) {
                payment.setStatus("REALIZED");
            }
            else if ("RETURNED".equalsIgnoreCase(status)) {
                payment.setStatus("RETURNED");

                // 1. Reverse Customer Debt
                Customer customer = order.getCustomer();
                customer.setDueBalance(customer.getDueBalance().add(payment.getAmount()));
                customerRepository.save(customer);

                // 2. Reverse Payment Record Due Balance
                payment.setDueBalance(payment.getDueBalance().add(payment.getAmount()));
            }

            salesOrderPaymentRepository.save(payment);

            // --- MASTER STATUS CALCULATION ---

            // Sum all actually cleared money (Cash or Realized Cheques)
            BigDecimal totalConfirmed = salesOrderPaymentRepository.findBySalesOrderId(order.getId())
                    .stream()
                    .filter(p -> "PAID".equalsIgnoreCase(p.getStatus()) || "REALIZED".equalsIgnoreCase(p.getStatus()))
                    .map(SalesOrderPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Check for other "in-flight" cheques
            boolean hasPending = salesOrderPaymentRepository
                    .existsBySalesOrderIdAndStatusIgnoreCase(order.getId(), "REALIZING");

            BigDecimal netTotal = calculateNetTotal(order.getGrandTotal(), order.getCourierCharges(),
                    order.getAdditionalDiscount(), order.getReturnCredits(),
                    order.getAdditionalDiscountType());

            // Logic to determine the correct label
            if (totalConfirmed.compareTo(netTotal) >= 0) {
                order.setPaymentStatus("PAID");
            }
            else if (totalConfirmed.compareTo(BigDecimal.ZERO) == 0 && !hasPending) {
                // If NO money has been cleared and NO cheques are pending, it's PENDING/UNPAID
                order.setPaymentStatus("PENDING");
            }
            else if (hasPending) {
                // If there are still cheques waiting in the bank, show REALIZING
                order.setPaymentStatus("REALIZING");
            }
            else {
                // If some money was cleared (e.g., a previous cash payment) but not all
                order.setPaymentStatus("PARTIAL");
            }

            salesOrderRepository.save(order);

            return Response.builder()
                    .status(200)
                    .message("Cheque " + status + ". Order status updated to " + order.getPaymentStatus())
                    .build();

        } catch (Exception e) {
            return Response.builder().status(500).message("Update failed: " + e.getMessage()).build();
        }
    }

    @Override
    public Response getCustomerSalesOrders(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        List<SalesOrder> salesOrders = salesOrderRepository.findBycustomer_idAndIsDeletedFalseAndStatusNot(customerId,SalesOrderStatus.Rejected);

        List<SalesOrderResponseDTO> salesOrderResponseDTOS = modelMapper.map(salesOrders, new TypeToken<List<SalesOrderResponseDTO>>() {}.getType());

        return Response.builder()
                .message("Returned data successfully")
                .status(200)
                .salesOrderList(salesOrderResponseDTOS)
                .build();
    }

    @Override
    public Response getSalesOrderById(Long orderId) {
        SalesOrder salesOrder = salesOrderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException("Order not found"));

        SalesOrderResponseDTO salesOrderResponseDTO = modelMapper.map(salesOrder,SalesOrderResponseDTO.class);

        return Response.builder()
                .status(200)
                .message("Successful")
                .salesOrder(salesOrderResponseDTO)
                .build();
    }

    @Override
    public Response updateSalesOrderDeliveryStatus(Long orderId) {
        SalesOrder salesOrder = salesOrderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException("Sales order not found"));

        salesOrder.setIsDelivered(Boolean.TRUE);
        salesOrderRepository.save(salesOrder);

        return Response.builder()
                .status(200)
                .message("Delivery status updated successfully")
                .build();
    }


    private SalesOrderResponseDTO mapToDTO(SalesOrder order) {

        SalesOrderPayment latestPayment = salesOrderPaymentRepository
                .findTopBySalesOrderIdOrderByIdDesc(order.getId());

        BigDecimal dueBalance;
        BigDecimal totalPaid;

        if (latestPayment != null) {
            dueBalance = latestPayment.getDueBalance();
            totalPaid = calculateNetTotal(order.getGrandTotal(),order.getCourierCharges(),order.getAdditionalDiscount(),order.getReturnCredits(),order.getAdditionalDiscountType()).subtract(dueBalance);
        } else {
            dueBalance = calculateNetTotal(order.getGrandTotal(),order.getCourierCharges(),order.getAdditionalDiscount(),order.getReturnCredits(),order.getAdditionalDiscountType());
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
                .returnCredits(order.getReturnCredits())
                .status(order.getStatus())
                .totalPaid(totalPaid)
                .dueBalance(dueBalance)
                .paymentStatus(order.getPaymentStatus())
                .netTotal(calculateNetTotal(order.getGrandTotal(),order.getCourierCharges(),order.getAdditionalDiscount(),order.getReturnCredits(),order.getAdditionalDiscountType()))
                .previousDueAmount(order.getCustomer().getDueBalance())
                .isDelivered(order.getIsDelivered() == null ?
                        Boolean.FALSE : order.getIsDelivered())
                .invoiceDueDate(
                        order.getCreditTerm() == null ? "N/A" :
                        "cash".equalsIgnoreCase(order.getCreditTerm())
                                ? "Cash"
                                : order.getInvoiceDate()
                                .plusDays(Long.parseLong(order.getCreditTerm()))
                                .toString()
                )
                .overDueOn(order.getOverDueOn())
                .daysRemaining(
                        order.getOverDueOn() == null ? 0 :
                                ChronoUnit.DAYS.between(LocalDate.now().atStartOfDay().toLocalDate(), order.getOverDueOn().atStartOfDay().toLocalDate()))

                .customer(CustomerDTO.builder()
                        .id(order.getCustomer().getId())
                        .name(order.getCustomer().getName())
                        .contact_No(order.getCustomer().getContact_No())
                        .email(order.getCustomer().getEmail())
                        .address(order.getCustomer().getAddress())
                        .creditLimit(order.getCustomer().getCreditLimit())
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
                                        .totalAmount( item.getDiscountedPrice().compareTo(BigDecimal.ZERO) > 0 ?
                                                item.getDiscountedPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                                                : item.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                        .discountPercent(item.getDiscountPercent())
                                        .discountedPrice(item.getDiscountedPrice())
                                        .unit(item.getUnit())
                                        .isReissue(item.getIsReissue())
                                        .returnQty(item.getReturnQty())
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
            BigDecimal returnCredits,
            DiscountTypeEnum discountType
    ) {
        if (grandTotal == null) grandTotal = BigDecimal.ZERO;
        if (courierCharges == null) courierCharges = BigDecimal.ZERO;
        if (discountValue == null) discountValue = BigDecimal.ZERO;
        if (returnCredits == null) returnCredits = BigDecimal.ZERO;

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
                .subtract(discountAmount)
                .subtract(returnCredits);
    }

    private void updatePendingReturnItems(Long customerId, Long productId, Integer quantityToReissue) {

        List<ProductReturnItem> pendingReturns = productReturnItemRepository
                .findByProductReturn_Customer_IdAndProduct_IdAndQuantityRemainingToReissueGreaterThan(customerId, productId, 0);

        int remainingToProcess = quantityToReissue;

        for (ProductReturnItem returnItem : pendingReturns) {
            if (remainingToProcess <= 0) break;

            int availableInThisReturn = returnItem.getQuantityRemainingToReissue();

            if (availableInThisReturn >= remainingToProcess) {
                returnItem.setQuantityRemainingToReissue(availableInThisReturn - remainingToProcess);

                if (returnItem.getQuantityRemainingToReissue() == 0) {
                    returnItem.setReissued(true);
                }

                remainingToProcess = 0;
            } else {
                remainingToProcess -= availableInThisReturn;
                returnItem.setQuantityRemainingToReissue(0);
                returnItem.setReissued(true);
            }

            productReturnItemRepository.save(returnItem);
        }
    }
    private void reverseReissueLogic(SalesOrder salesOrder) {
        for (SalesOrderItem item : salesOrder.getItems()) {
            if (Boolean.TRUE.equals(item.getIsReissue())) {
                // Find the most recently updated return items to "refill" them
                List<ProductReturnItem> returnItems = productReturnItemRepository
                        .findTopByProductReturn_Customer_IdAndProduct_IdOrderByProductReturn_ReturnDateDesc(
                                salesOrder.getCustomer().getId(),
                                item.getProduct().getId()
                        );

                if (!returnItems.isEmpty()) {
                    ProductReturnItem returnItem = returnItems.getFirst();
                    returnItem.setQuantityRemainingToReissue(returnItem.getQuantityRemainingToReissue() + item.getQuantity());
                    returnItem.setReissued(false);
                    productReturnItemRepository.save(returnItem);
                }
            }
        }
    }

}
