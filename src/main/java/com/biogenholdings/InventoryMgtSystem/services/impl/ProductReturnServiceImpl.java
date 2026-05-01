package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.enums.SalesOrderStatus;
import com.biogenholdings.InventoryMgtSystem.exceptions.NotFoundException;
import com.biogenholdings.InventoryMgtSystem.models.*;
import com.biogenholdings.InventoryMgtSystem.repositories.*;
import com.biogenholdings.InventoryMgtSystem.services.ProductReturnService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j

public class ProductReturnServiceImpl implements ProductReturnService {
    private final SalesOrderRepository salesOrderRepository;
    private final ProductReturnRepository productReturnRepository;
    private final CustomerRepository customerRepository;
    private final ProductStockRepository productStockRepository;
    private final UserRepository userRepository;
    private final ProductReturnItemRepository productReturnItemRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;

    @Transactional
    @Override
    public Response processProductReturn(ProductReturnRequestDTO request) {

        SalesOrder order = salesOrderRepository.findById(request.getSalesOrderId())
                .orElseThrow(() -> new RuntimeException("Original Sales Order not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not Found"));

        ProductReturn productReturn = ProductReturn.builder()
                .returnNumber("PENDING-" + System.currentTimeMillis())
                .salesOrder(order)
                .customer(order.getCustomer())
                .salesRep(order.getUser())
                .returnDate(LocalDateTime.now())
                .remarks(request.getRemarks())
                .createdBy(user)
                .status(SalesOrderStatus.Pending)
                .returnItems(new ArrayList<>())
                .totalReturnAmount(BigDecimal.ZERO)
                .totalCommissionReversal(BigDecimal.ZERO)
                .isDeleted(false)
                .build();

        BigDecimal headerTotalReturn = BigDecimal.ZERO;
        BigDecimal headerTotalCommReversal = BigDecimal.ZERO;

        for (ReturnItemRequestDTO itemRequest : request.getItems()) {

            SalesOrderItem originalItem = order.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(itemRequest.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product " + itemRequest.getProductId() + " was not in original invoice"));

            int alreadyReturnedQty = (originalItem.getReturnQty() != null) ? originalItem.getReturnQty() : 0;

            if (itemRequest.getQuantity() > (originalItem.getQuantity() - alreadyReturnedQty)) {
                throw new RuntimeException("Return quantity exceeds remaining returnable quantity for " + originalItem.getProduct().getName());
            }

            Integer pendingResult = productReturnItemRepository.sumQtyBySalesOrderAndProductAndStatus(
                    order.getId(),
                    itemRequest.getProductId());

            int otherPendingQty = (pendingResult != null) ? pendingResult : 0;

            int maxAllowed = originalItem.getQuantity() - alreadyReturnedQty - otherPendingQty;

            if (itemRequest.getQuantity() > maxAllowed) {
                throw new RuntimeException("Limit exceeded. " + otherPendingQty + " units are pending in another note.");
            }

            BigDecimal unitPrice = originalItem.getSellingPrice();
            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            BigDecimal commReversal = BigDecimal.ZERO;

            if (Boolean.TRUE.equals(itemRequest.getIsReusable())) {
                BigDecimal commRate = originalItem.getProduct().getSRepCommissionRate();
                commReversal = (commRate != null)
                        ? subTotal.multiply(commRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                log.info("Reusable item detected. Commission reversal of {} calculated for product: {}",
                        commReversal, originalItem.getProduct().getName());
            } else {
                log.info("Non-reusable item detected. Skipping commission reversal for product: {}",
                        originalItem.getProduct().getName());
            }

            ProductReturnItem returnItem = ProductReturnItem.builder()
                    .productReturn(productReturn)
                    .product(originalItem.getProduct())
                    .quantity(itemRequest.getQuantity())
                    .unitPriceAtReturn(unitPrice)
                    .subTotal(subTotal)
                    .isReusable(itemRequest.getIsReusable())
                    .returnReason(itemRequest.getReason())
                    .commissionReversalAmount(commReversal)
                    .quantityRemainingToReissue(0)
                    .build();

            productReturn.getReturnItems().add(returnItem);
            headerTotalReturn = headerTotalReturn.add(subTotal);
            headerTotalCommReversal = headerTotalCommReversal.add(commReversal);
        }

        productReturn.setTotalReturnAmount(headerTotalReturn);
        productReturn.setTotalCommissionReversal(headerTotalCommReversal);

        ProductReturn saved = productReturnRepository.save(productReturn);

        return Response.builder()
                .status(200)
                .message("Return request " + saved.getId() + " created. Awaiting approval.")
                .build();
    }

    @Override
    public Response findReturnById(String returnId) {
        ProductReturn productReturn = productReturnRepository.findByReturnNumber(returnId)
                .orElseThrow(() -> new NotFoundException("Return not found with Number: " + returnId));

        ProductReturnResponseDTO invoiceData = ProductReturnResponseDTO.builder()
                .returnNumber(productReturn.getReturnNumber())
                .returnDate(productReturn.getReturnDate())
                .originalInvoiceNumber(productReturn.getSalesOrder().getInvoiceNumber())
                .remarks(productReturn.getRemarks())
                .processedBy(productReturn.getCreatedBy().getName())

                .customerName(productReturn.getCustomer().getName())
                .customerAddress(productReturn.getCustomer().getAddress())
                .customerPhone(productReturn.getCustomer().getContact_No())

                .salesRepName(productReturn.getSalesRep().getName())

                .items(productReturn.getReturnItems().stream().map(item ->
                        ReturnItemDetailDTO.builder()
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPriceAtReturn())
                                .subTotal(item.getSubTotal())
                                .reason(item.getReturnReason())
                                .isReusable(item.getIsReusable())
                                .build()
                ).collect(Collectors.toList()))

                .totalReturnAmount(productReturn.getTotalReturnAmount())
                .totalCommissionReversal(productReturn.getTotalCommissionReversal())
                .build();

        return Response.builder()
                .status(200)
                .message("Return details fetched successfully")
                .productReturn(invoiceData)
                .build();
    }

    @Override
    public Response findAllReturns(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("returnDate").descending());
        Page<ProductReturn> returnsPage = productReturnRepository.findByIsDeletedFalse(pageable);

        List<ProductReturnResponseDTO> dtoList = returnsPage.getContent().stream()
                .map(ret -> ProductReturnResponseDTO.builder()
                        .returnNumber(ret.getReturnNumber())
                        .originalInvoiceNumber(ret.getSalesOrder().getInvoiceNumber())
                        .customerName(ret.getCustomer().getName())
                        .salesRepName(ret.getSalesRep().getName())
                        .returnDate(ret.getReturnDate())
                        .totalReturnAmount(ret.getTotalReturnAmount())
                        .remarks(ret.getRemarks())
                        .status(ret.getStatus())
                        .items(ret.getReturnItems().stream().map(item ->
                                ReturnItemDetailDTO.builder()
                                        .productName(item.getProduct().getName())
                                        .quantity(item.getQuantity())
                                        .unitPrice(item.getUnitPriceAtReturn())
                                        .subTotal(item.getSubTotal())
                                        .isReusable(item.getIsReusable())
                                        .reason(item.getReturnReason())
                                        .build()
                        ).collect(Collectors.toList()))
                        .build()
                ).collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Returns fetched successfully")
                .productReturnList(dtoList)
                .totalElements(returnsPage.getTotalElements())
                .totalPages(returnsPage.getTotalPages())
                .build();
    }

    @Override
    public Response getCustomerReturnSummary(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + customerId));

        List<ProductReturnItem> pendingItems = productReturnItemRepository
                .findByProductReturn_Customer_IdAndQuantityRemainingToReissueGreaterThan(customerId, 0);

        List<ReturnItemDetailDTO> aggregatedProducts = new ArrayList<>(pendingItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Product p = list.getFirst().getProduct();
                                    Integer totalQty = list.stream()
                                            .mapToInt(ProductReturnItem::getQuantityRemainingToReissue)
                                            .sum();

                                    return ReturnItemDetailDTO.builder()
                                            .productId(p.getId())
                                            .productName(p.getName())
                                            .quantity(totalQty)
                                            .units(p.getUnit())
                                            .packSize(p.getPackSize())
                                            .build();
                                }
                        )
                ))
                .values());

        CustomerWiseReturnItemDTO summary = CustomerWiseReturnItemDTO.builder()
                .customerCurrentDue(customer.getAvailableReturnCredit())
                .returnProducts(aggregatedProducts)
                .build();

        return Response.builder()
                .status(200)
                .message("Customer return summary fetched successfully")
                .customerWiseReturnItemDTO(summary)
                .build();
    }

    @Transactional
    @Override
    public Response deleteReturnInvoice(String returnId, Long userId) {

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User Not found"));

        ProductReturn productReturn = productReturnRepository.findByReturnNumber(returnId)
                .orElseThrow(() -> new NotFoundException("Return Invoice not found"));

        if (Boolean.TRUE.equals(productReturn.getIsDeleted())) {
            throw new RuntimeException("Already deleted.");
        }

        if (productReturn.getStatus() != SalesOrderStatus.Pending) {
            throw new RuntimeException("Cannot delete return. Only PENDING returns can be deleted. Approved returns must remain for auditing.");
        }

        productReturn.setIsDeleted(true);
        productReturn.setDeletedBy(currentUser);
        productReturn.setDeletedAt(LocalDateTime.now());
        productReturn.setStatus(SalesOrderStatus.Deleted);

        productReturnRepository.save(productReturn);

        return Response.builder()
                .status(200)
                .message("Pending Return " + productReturn.getReturnNumber() + " has been successfully deleted/voided.")
                .build();
    }

    @Transactional
    @Override
    public Response approveReturn(String returnId, Long userId) {
        User approvedBy = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ProductReturn productReturn = productReturnRepository.findByReturnNumber(returnId)
                .orElseThrow(() -> new NotFoundException("Return record not found"));

        if (productReturn.getStatus() != SalesOrderStatus.Pending) {
            throw new RuntimeException("Only pending returns can be approved.");
        }

        BigDecimal amountToReduceFromDue = BigDecimal.ZERO;

        for (ProductReturnItem item : productReturn.getReturnItems()) {

            SalesOrderItem originalItem = productReturn.getSalesOrder().getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(item.getProduct().getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Original order link broken"));

            int currentReturnQty = (originalItem.getReturnQty() != null) ? originalItem.getReturnQty() : 0;
            originalItem.setReturnQty(currentReturnQty + item.getQuantity());
            salesOrderItemRepository.save(originalItem);

            if (item.getIsReusable()) {

                ProductStock stock = productStockRepository.findByProductId(item.getProduct().getId())
                        .orElseThrow(() -> new NotFoundException("Stock not found"));
                stock.setTotalQuantity(stock.getTotalQuantity() + item.getQuantity());
                productStockRepository.save(stock);

                amountToReduceFromDue = amountToReduceFromDue.add(item.getSubTotal());
                item.setQuantityRemainingToReissue(0);
            } else {

                item.setQuantityRemainingToReissue(item.getQuantity());
            }
        }

        Customer customer = productReturn.getCustomer();

        if (amountToReduceFromDue.compareTo(BigDecimal.ZERO) > 0) {
            customer.setDueBalance(customer.getDueBalance().subtract(amountToReduceFromDue));
            BigDecimal creditLimit = customer.getAvailableReturnCredit() != null ? customer.getAvailableReturnCredit() : BigDecimal.ZERO;
            customer.setAvailableReturnCredit(creditLimit.add(amountToReduceFromDue));
            customerRepository.save(customer);
        }

        productReturn.setStatus(SalesOrderStatus.Approved);
        productReturn.setReturnNumber("RET-" + String.format("%03d", productReturn.getId()));
        productReturn.setApprovedBy(approvedBy);
        productReturn.setApprovedAt(LocalDateTime.now());

        productReturnRepository.save(productReturn);

        return Response.builder()
                .status(200)
                .message("Return approved. Stock updated and RS. " + amountToReduceFromDue + " credited to customer.")
                .build();
    }

}

