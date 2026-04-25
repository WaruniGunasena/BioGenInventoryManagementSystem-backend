package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.Response;

public interface CommissionService {

    Response getMyCommissions (Long userId);
}
