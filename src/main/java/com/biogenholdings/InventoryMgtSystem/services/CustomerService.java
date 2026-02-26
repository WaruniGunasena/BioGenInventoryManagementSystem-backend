package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.CustomerDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.enums.FilterEnum;

public interface CustomerService {

    Response addCustomer(CustomerDTO customerDTO);

    Response getCustomer(Long customerId);

    Response getAllCustomers();

    Response getAllCustomersPaginated(Integer page, Integer size, FilterEnum filter);

    Response updateCustomer(Long CustomerId,CustomerDTO customerDTO);

    Response deleteCustomer(Long customerId);

    Response softDeleteCustomer(Long customerId, Long userId);

    Response searchCustomer(String name);
}
