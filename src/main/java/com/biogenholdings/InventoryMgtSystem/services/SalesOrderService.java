package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.enums.SalesOrderStatus;
import com.biogenholdings.InventoryMgtSystem.models.SalesOrderPayment;

import java.util.List;

public interface SalesOrderService {

    SalesOrderResponseDTO createSalesOrder(SalesOrderRequestDTO request);

    String generateInvoiceNumber();

    Response getPaginatedSalesOrders(int page, int size);

    Response softDeleteSalesOrder(Long salesOrderId, Long userId);

    SalesOrderResponseDTO updateSalesOrder(Long orderId, SalesOrderRequestDTO request, Long userId);

    Response approveSalesOrder(SalesOrderStatus salesOrderStatus, Long userId, Long salesOrderId);

    Long pendingSalesOrderCount();

    Response createSalesOrderPayment(SalesOrderPaymentDTO dto);

    Response getCustomerSalesOrders(Long customerId);

    Response getSalesOrderById(Long orderId);

    Response updateSalesOrderDeliveryStatus(Long orderId);

    Response processChequeStatus(Long salesOrderId, String status);

    List<SalesOrderPayment> getPendingChequesByOrderId(Long salesOrderId);
}
