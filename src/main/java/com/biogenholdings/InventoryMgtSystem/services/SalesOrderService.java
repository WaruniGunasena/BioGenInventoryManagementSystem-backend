package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.enums.SalesOrderStatus;

public interface SalesOrderService {

    SalesOrderResponseDTO createSalesOrder(SalesOrderRequestDTO request);

    String generateInvoiceNumber();

    Response getPaginatedSalesOrders(int page, int size);

    Response softDeleteSalesOrder(Long salesOrderId, Long userId);

    SalesOrderResponseDTO updateSalesOrder(Long orderId, SalesOrderRequestDTO request, Long userId);

    Response approveSalesOrder(SalesOrderStatus salesOrderStatus, Long userId, Long salesOrderId);

    Long pendingSalesOrderCount();

    Response createSalesOrderPayment(SalesOrderPaymentDTO dto);
}
