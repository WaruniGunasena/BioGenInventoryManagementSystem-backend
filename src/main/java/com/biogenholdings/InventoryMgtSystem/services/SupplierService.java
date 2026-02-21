package com.biogenholdings.InventoryMgtSystem.services;


import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.dtos.SupplierDTO;

public interface SupplierService {

    Response addSupplier(SupplierDTO supplierDTO);

    Response getAllSupplier();

    Response getSupplierById(Long id);

    Response updateSupplier(Long id, SupplierDTO supplierDTO);

    Response deleteSupplier( Long id);

    Response searchSupplier(String searchKey);

}
