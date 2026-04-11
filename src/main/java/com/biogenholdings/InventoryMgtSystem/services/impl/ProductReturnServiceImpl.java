package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
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
    private final ProductStockRepository productStock;
    private final UserRepository userRepository;
    private final ProductReturnItemRepository productReturnItemRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;

    @Transactional
    @Override
    public Response processProductReturn(ProductReturnRequestDTO request) {
        // 1. Fetch the Original Sales Order
        SalesOrder order = salesOrderRepository.findById(request.getSalesOrderId())
                .orElseThrow(() -> new RuntimeException("Original Sales Order not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not Found"));

        // 2. Initialize the Header
        ProductReturn productReturn = ProductReturn.builder()
                .returnNumber(String.valueOf(System.currentTimeMillis()))
                .salesOrder(order)
                .customer(order.getCustomer())
                .salesRep(order.getUser())
                .returnDate(LocalDateTime.now())
                .remarks(request.getRemarks())
                .createdBy(user)
                .returnItems(new ArrayList<>())
                .totalReturnAmount(BigDecimal.ZERO)
                .totalCommissionReversal(BigDecimal.ZERO)
                .build();

        BigDecimal headerTotalReturn = BigDecimal.ZERO;
        BigDecimal headerTotalCommReversal = BigDecimal.ZERO;
        BigDecimal amountToReduceFromDue = BigDecimal.ZERO;

        for (ReturnItemRequestDTO itemRequest : request.getItems()) {
            // 3. Find original item & validate quantity
            SalesOrderItem originalItem = order.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(itemRequest.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product " + itemRequest.getProductId() + " was not in original invoice"));

            if (itemRequest.getQuantity() > originalItem.getQuantity()) {
                throw new RuntimeException("Return quantity exceeds original purchased quantity for " + originalItem.getProduct().getName());
            }

            // 4. Calculate Values
            BigDecimal unitPrice = originalItem.getSellingPrice();
            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            BigDecimal commRate = originalItem.getProduct().getSRepCommissionRate();
            BigDecimal commReversal = (commRate != null)
                    ? subTotal.multiply(commRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            originalItem.setReturnQty(originalItem.getReturnQty() + itemRequest.getQuantity());
            salesOrderItemRepository.save(originalItem);

            // 5. Create Return Item Entity with Reissue Logic
            ProductReturnItem returnItem = ProductReturnItem.builder()
                    .productReturn(productReturn)
                    .product(originalItem.getProduct())
                    .quantity(itemRequest.getQuantity())
                    .unitPriceAtReturn(unitPrice)
                    .subTotal(subTotal)
                    .isReusable(itemRequest.getIsReusable())
                    .returnReason(itemRequest.getReason())
                    .commissionReversalAmount(commReversal)
                    .build();

            // LOGIC: If NOT reusable, set the quantity available for free reissue later
            if (!itemRequest.getIsReusable()) {
                returnItem.setQuantityRemainingToReissue(itemRequest.getQuantity());
            } else {
                returnItem.setQuantityRemainingToReissue(0);
                // Accumulate amount to subtract from customer debt
                amountToReduceFromDue = amountToReduceFromDue.add(subTotal);

                // INVENTORY LOGIC: Restock only if Reusable
                ProductStock product = productStock.findByProductId(itemRequest.getProductId())
                        .orElseThrow(()-> new NotFoundException("Product Not Found"));
                product.setTotalQuantity(product.getTotalQuantity() + itemRequest.getQuantity());
                productStock.save(product);
            }

            productReturn.getReturnItems().add(returnItem);
            headerTotalReturn = headerTotalReturn.add(subTotal);
            headerTotalCommReversal = headerTotalCommReversal.add(commReversal);
        }
        Customer customer = order.getCustomer();
        // 6. Update Customer Balance (Reduction for Reusable items)
        if (amountToReduceFromDue.compareTo(BigDecimal.ZERO) > 0) {

            // Ensure balance doesn't go below zero if that's your business rule
            customer.setDueBalance(customer.getDueBalance().subtract(amountToReduceFromDue));

        }

        // 7. Finalize Header Totals and Save
        productReturn.setTotalReturnAmount(headerTotalReturn);
        productReturn.setTotalCommissionReversal(headerTotalCommReversal);
        customer.setAvailableReturnCredit(
                customer.getAvailableReturnCredit().add(amountToReduceFromDue)
        );



        ProductReturn saved = productReturnRepository.save(productReturn);
        saved.setReturnNumber("RET-" + String.format("%03d", saved.getId()));
        customerRepository.save(customer);
        productReturnRepository.save(saved);

        return Response.builder()
                .status(200)
                .message("Return " + productReturn.getReturnNumber() + " processed. " +
                        (amountToReduceFromDue.compareTo(BigDecimal.ZERO) > 0 ? "Debt reduced by " + amountToReduceFromDue : ""))
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
        Page<ProductReturn> returnsPage = productReturnRepository.findAll(pageable);

        List<ProductReturnResponseDTO> dtoList = returnsPage.getContent().stream()
                .map(ret -> ProductReturnResponseDTO.builder()
                        .returnNumber(ret.getReturnNumber())
                        .originalInvoiceNumber(ret.getSalesOrder().getInvoiceNumber())
                        .customerName(ret.getCustomer().getName())
                        .salesRepName(ret.getSalesRep().getName())
                        .returnDate(ret.getReturnDate())
                        .totalReturnAmount(ret.getTotalReturnAmount())
                        .remarks(ret.getRemarks())
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
}

