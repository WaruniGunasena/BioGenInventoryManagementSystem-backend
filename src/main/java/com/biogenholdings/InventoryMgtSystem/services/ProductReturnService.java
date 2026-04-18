package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.ProductReturnRequestDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.models.User;

public interface ProductReturnService {

    Response processProductReturn(ProductReturnRequestDTO request);
    Response findReturnById(String ReturnId);
    Response findAllReturns(int page, int size);
    Response getCustomerReturnSummary(Long customerId);
    Response deleteReturnInvoice(String returnId, Long userId);
    Response approveReturn(String returnId, Long userId);
}
