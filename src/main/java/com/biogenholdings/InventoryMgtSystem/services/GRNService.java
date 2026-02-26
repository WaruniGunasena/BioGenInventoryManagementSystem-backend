package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.GRNRequestDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;

public interface GRNService {

    Response createGRN(GRNRequestDTO grnRequestDTO);

    Response getAllGRNs();

    Response getGRNById(Long id);

    Response getGRNBySupplier(Long supplierId);
}