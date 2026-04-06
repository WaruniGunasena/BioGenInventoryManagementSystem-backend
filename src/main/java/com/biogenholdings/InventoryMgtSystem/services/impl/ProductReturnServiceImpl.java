package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.ProductReturnRequestDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.models.*;
import com.biogenholdings.InventoryMgtSystem.repositories.ProductRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.ProductReturnRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.SalesOrderRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.UserRepository;
import com.biogenholdings.InventoryMgtSystem.services.ProductReturnService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ProductReturnServiceImpl implements ProductReturnService {
    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final ProductReturnRepository productReturnRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public Response processProductReturn(ProductReturnRequestDTO request, User currentUser) {
        // 1. Fetch the Original Sales Order
//        SalesOrder order = salesOrderRepository.findById(request.getSalesOrderId())
//                .orElseThrow(() -> new NotFoundException("Original Sales Order not found"));
//
//        // 2. Initialize the Header
//        ProductReturn productReturn = ProductReturn.builder()
//                .returnNumber("RET-" + System.currentTimeMillis()) // Professional unique ID
//                .salesOrder(order)
//                .customer(order.getCustomer())
//                .salesRep(order.getUser()) // Assuming 'user' in SalesOrder is the Rep
//                .returnDate(LocalDateTime.now())
//                .remarks(request.getRemarks())
//                .createdBy(currentUser)
//                .returnItems()
//                .totalReturnAmount(BigDecimal.ZERO)
//                .totalCommissionReversal(BigDecimal.ZERO)
//                .build();
//
//        BigDecimal headerTotalReturn = BigDecimal.ZERO;
//        BigDecimal headerTotalCommReversal = BigDecimal.ZERO;
//
//        for (ReturnItemRequestDTO itemRequest : request.getItems()) {
//            // 3. Find the item in the original order to get the PAID price
//            SalesOrderItem originalItem = order.getItems().stream()
//                    .filter(i -> i.getProduct().getId().equals(itemRequest.getProductId()))
//                    .findFirst()
//                    .orElseThrow(() -> new BadRequestException("Product was not in original invoice"));
//
//            // 4. Calculate Values
//            BigDecimal unitPrice = originalItem.getUnitPrice();
//            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
//
//            // Calculate Commission Reversal (using rate from original Product)
//            BigDecimal commRate = originalItem.getProduct().getSRepCommissionRate();
//            BigDecimal commReversal = (commRate != null)
//                    ? subTotal.multiply(commRate).divide(BigDecimal.valueOf(100))
//                    : BigDecimal.ZERO;
//
//            // 5. Create Return Item Entity
//            ProductReturnItem returnItem = ProductReturnItem.builder()
//                    .productReturn(productReturn)
//                    .product(originalItem.getProduct())
//                    .quantity(itemRequest.getQuantity())
//                    .unitPriceAtReturn(unitPrice)
//                    .subTotal(subTotal)
//                    .isReusable(itemRequest.getIsReusable())
//                    .returnReason(itemRequest.getReason())
//                    .commissionReversalAmount(commReversal)
//                    .build();
//
//            productReturn.getReturnItems().add(returnItem);
//            headerTotalReturn = headerTotalReturn.add(subTotal);
//            headerTotalCommReversal = headerTotalCommReversal.add(commReversal);
//
//            // 6. INVENTORY LOGIC: Only restock if Reusable
//            if (itemRequest.getIsReusable()) {
//                Product product = originalItem.getProduct();
//                product.setOpeningBalance(product.getOpeningBalance() + itemRequest.getQuantity());
//                productRepository.save(product);
//            }
//        }
//
//        // 7. Finalize Header Totals and Save
//        productReturn.setTotalReturnAmount(headerTotalReturn);
//        productReturn.setTotalCommissionReversal(headerTotalCommReversal);
//
//        productReturnRepository.save(productReturn);
//
//        // 8. OPTIONAL: Update SalesOrder status or Due Balance here
//        // If your SalesOrder has a 'dueAmount', subtract headerTotalReturn from it.

        return Response.builder()
                .status(200)
//                .message("Return " + productReturn.getReturnNumber() + " processed successfully.")
                .build();
    }
}

