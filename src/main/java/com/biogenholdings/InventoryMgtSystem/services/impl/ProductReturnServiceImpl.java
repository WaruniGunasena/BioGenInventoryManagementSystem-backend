package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.enums.SalesOrderStatus;
import com.biogenholdings.InventoryMgtSystem.exceptions.NotFoundException;
import com.biogenholdings.InventoryMgtSystem.models.*;
import com.biogenholdings.InventoryMgtSystem.repositories.*;
import com.biogenholdings.InventoryMgtSystem.services.ProductReturnService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
public class ProductReturnServiceImpl implements ProductReturnService {
    private final SalesOrderRepository salesOrderRepository;
    private final ProductReturnRepository productReturnRepository;
    private final CustomerRepository customerRepository; // Added for debt reduction
    private final ProductStockRepository productStockRepository;
    private final UserRepository userRepository;
    private final ProductReturnItemRepository productReturnItemRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;

    @Transactional
    @Override
    public Response processProductReturn(ProductReturnRequestDTO request) {
        // 1. Fetch the Original Sales Order & User
        SalesOrder order = salesOrderRepository.findById(request.getSalesOrderId())
                .orElseThrow(() -> new RuntimeException("Original Sales Order not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not Found"));

        // 2. Initialize the Header with Status PENDING
        ProductReturn productReturn = ProductReturn.builder()
                .returnNumber("PENDING-" + System.currentTimeMillis())
                .salesOrder(order)
                .customer(order.getCustomer())
                .salesRep(order.getUser())
                .returnDate(LocalDateTime.now())
                .remarks(request.getRemarks())
                .createdBy(user)
                .status(SalesOrderStatus.Pending) // Make sure you have this Enum
                .returnItems(new ArrayList<>())
                .totalReturnAmount(BigDecimal.ZERO)
                .totalCommissionReversal(BigDecimal.ZERO)
                .isDeleted(false)
                .build();

        BigDecimal headerTotalReturn = BigDecimal.ZERO;
        BigDecimal headerTotalCommReversal = BigDecimal.ZERO;

        for (ReturnItemRequestDTO itemRequest : request.getItems()) {

            // 3. Find original item & validate quantity
            SalesOrderItem originalItem = order.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(itemRequest.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product " + itemRequest.getProductId() + " was not in original invoice"));

            if (itemRequest.getQuantity() > (originalItem.getQuantity() - originalItem.getReturnQty())) {
                throw new RuntimeException("Return quantity exceeds remaining returnable quantity for " + originalItem.getProduct().getName());
            }

            Integer otherPendingQty = productReturnItemRepository.sumQtyBySalesOrderAndProductAndStatus(
                    order.getId(),
                    itemRequest.getProductId()
            );

            // 2. Real Available = Original - (Already Approved) - (Other Pending)
            if(otherPendingQty != null){
                int maxAllowed = originalItem.getQuantity() - originalItem.getReturnQty() - otherPendingQty;

                if (itemRequest.getQuantity() > maxAllowed) {
                    throw new RuntimeException("Can not update max limit exceeded: " + originalItem.getProduct().getName() +
                            ". You already have " + otherPendingQty + " units pending in another return. " +
                            "Max available to return now is: " + maxAllowed + ". Please delete the existing pending note to proceed.");
                }
            }

            // 4. Calculate Values
            BigDecimal unitPrice = originalItem.getSellingPrice();
            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            BigDecimal commRate = originalItem.getProduct().getSRepCommissionRate();
            BigDecimal commReversal = (commRate != null)
                    ? subTotal.multiply(commRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // 5. Create Return Item Entity (NO quantityRemainingToReissue update yet!)
            ProductReturnItem returnItem = ProductReturnItem.builder()
                    .productReturn(productReturn)
                    .product(originalItem.getProduct())
                    .quantity(itemRequest.getQuantity())
                    .unitPriceAtReturn(unitPrice)
                    .subTotal(subTotal)
                    .isReusable(itemRequest.getIsReusable())
                    .returnReason(itemRequest.getReason())
                    .commissionReversalAmount(commReversal)
                    .quantityRemainingToReissue(0) // Logic moved to Approve
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

        // Map Entity to Invoice DTO
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
                .productReturn(invoiceData) // Assuming your Response class has an 'Object data' field
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
                        // Mapping items for the "View" modal
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
        // 1. Fetch the Customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + customerId));

        // 2. Fetch all individual pending return line items
        List<ProductReturnItem> pendingItems = productReturnItemRepository
                .findByProductReturn_Customer_IdAndQuantityRemainingToReissueGreaterThan(customerId, 0);

        // 3. Logic to Group by Product and Sum the Quantities
        List<ReturnItemDetailDTO> aggregatedProducts = new ArrayList<>(pendingItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getId(), // Group by ID
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    // Take product info from the first item in the group
                                    Product p = list.getFirst().getProduct();
                                    // Sum all quantities in this group
                                    Integer totalQty = list.stream()
                                            .mapToInt(ProductReturnItem::getQuantityRemainingToReissue)
                                            .sum();

                                    return ReturnItemDetailDTO.builder()
                                            .productId(p.getId())
                                            .productName(p.getName())
                                            .quantity(totalQty)
                                            .units(p.getUnit())
                                            .build();
                                }
                        )
                ))
                .values());

        // 4. Map to the Final DTO
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

        // 1. Fetch the Return Record
        ProductReturn productReturn = productReturnRepository.findByReturnNumber(returnId)
                .orElseThrow(() -> new NotFoundException("Return Invoice not found"));

        // 2. Prevent double-deletion
        if (Boolean.TRUE.equals(productReturn.getIsDeleted())) {
            throw new RuntimeException("This return invoice is already deleted.");
        }

        // 3. CRITICAL VALIDATION: Only allow deletion if status is PENDING
        // Since Approved ones aren't deletable, we don't need to reverse stock or dueBalance
        if (productReturn.getStatus() != SalesOrderStatus.Pending) {
            throw new RuntimeException("Cannot delete return. Only PENDING returns can be deleted. Approved returns must remain for auditing.");
        }

        // 4. PERFORM SOFT DELETE
        // No math needed here because PENDING returns haven't affected stock or finances yet
        productReturn.setIsDeleted(true);
        productReturn.setDeletedBy(currentUser);
        productReturn.setDeletedAt(LocalDateTime.now());
        productReturn.setStatus(SalesOrderStatus.Deleted); // Optional: explicitly set a VOIDED status

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

            // 1. Update original SalesOrder item so we know it's returned
            SalesOrderItem originalItem = productReturn.getSalesOrder().getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(item.getProduct().getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Original order link broken"));

            originalItem.setReturnQty(originalItem.getReturnQty() + item.getQuantity());
            salesOrderItemRepository.save(originalItem);

            // 2. Logic for Reissue vs Restock
            if (item.getIsReusable()) {
                // RESTOCK Logic
                ProductStock stock = productStockRepository.findByProductId(item.getProduct().getId())
                        .orElseThrow(() -> new NotFoundException("Stock not found"));
                stock.setTotalQuantity(stock.getTotalQuantity() + item.getQuantity());
                productStockRepository.save(stock);

                // This amount will reduce the customer's cash debt
                amountToReduceFromDue = amountToReduceFromDue.add(item.getSubTotal());
                item.setQuantityRemainingToReissue(0);
            } else {
                // REISSUE Logic: Item is now officially a "Voucher" for the customer
                item.setQuantityRemainingToReissue(item.getQuantity());
            }
        }

        // 3. Update Customer Finances
        Customer customer = productReturn.getCustomer();
        BigDecimal creditLimit = customer.getAvailableReturnCredit() != null ? customer.getCreditLimit() : BigDecimal.ZERO;
        if (amountToReduceFromDue.compareTo(BigDecimal.ZERO) > 0) {
            customer.setDueBalance(customer.getDueBalance().subtract(amountToReduceFromDue));
            customer.setAvailableReturnCredit(creditLimit.add(amountToReduceFromDue));
            customerRepository.save(customer);
        }

        // 4. Finalize Return Status
        productReturn.setStatus(SalesOrderStatus.Approved);
        productReturn.setReturnNumber("RET-" + String.format("%03d", productReturn.getId()));
        productReturn.setApprovedBy(approvedBy); // Assuming you add this column
        productReturn.setApprovedAt(LocalDateTime.now());

        productReturnRepository.save(productReturn);

        return Response.builder()
                .status(200)
                .message("Return approved. Stock updated and RS. " + amountToReduceFromDue + " credited to customer.")
                .build();
    }

}

