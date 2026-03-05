package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.dtos.SalesOrderRequestDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.SalesOrderResponseDTO;

public interface SalesOrderService {

    SalesOrderResponseDTO createSalesOrder(SalesOrderRequestDTO request);

    String generateInvoiceNumber();

    Response getPaginatedSalesOrders(int page, int size);
}
