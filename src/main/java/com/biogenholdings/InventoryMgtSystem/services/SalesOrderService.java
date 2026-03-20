package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.dtos.SalesOrderItemResponseDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.SalesOrderRequestDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.SalesOrderResponseDTO;
import com.biogenholdings.InventoryMgtSystem.enums.SalesOrderStatus;

public interface SalesOrderService {

    SalesOrderResponseDTO createSalesOrder(SalesOrderRequestDTO request);

    String generateInvoiceNumber();

    Response getPaginatedSalesOrders(int page, int size);

    Response softDeleteSalesOrder(Long salesOrderId, Long userId);

    SalesOrderResponseDTO updateSalesOrder(Long orderId, SalesOrderRequestDTO request, Long userId);

    Response approveSalesOrder(SalesOrderStatus salesOrderStatus, Long userId, Long salesOrderId);
}
