package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.ProductReturnRequestDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.models.User;

public interface ProductReturnService {

    Response processProductReturn(ProductReturnRequestDTO request, User currentUser);
}
