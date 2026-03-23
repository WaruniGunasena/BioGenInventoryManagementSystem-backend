package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.GRNPaymentDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.GRNRequestDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;

public interface GRNService {

    Response createGRN(GRNRequestDTO grnRequestDTO);

    Response getAllGRNs();

    Response getGRNById(Long id);

    Response getGRNBySupplier(Long supplierId);

    Response getPaginatedGRNs(int page, int size);

    Response updateGRN(Long id, GRNRequestDTO dto);

    Response softDeleteGRN(Long id, Long userId);

    Response createGRNPayment(GRNPaymentDTO dto);

}