package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.CustomerDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.enums.FilterEnum;
import com.biogenholdings.InventoryMgtSystem.exceptions.NotFoundException;
import com.biogenholdings.InventoryMgtSystem.exceptions.UserExistException;
import com.biogenholdings.InventoryMgtSystem.models.Customer;
import com.biogenholdings.InventoryMgtSystem.models.User;
import com.biogenholdings.InventoryMgtSystem.repositories.CustomerRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.UserRepository;
import com.biogenholdings.InventoryMgtSystem.services.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response addCustomer(CustomerDTO customerDTO) {
        Customer customer = customerRepository.findByEmail(customerDTO.getEmail())
                .orElseThrow(()-> new UserExistException("Customer Already Exists"));

        Customer customerToSave = Customer.builder()
                .email(customerDTO.getEmail())
                .address(customerDTO.getAddress())
                .name(customerDTO.getName())
                .postalCode(customerDTO.getPostalCode())
                .contact_No(customerDTO.getContact_No())
                .province(customerDTO.getProvince())
                .build();

        customerRepository.save(customerToSave);

        return Response.builder()
                .status(200)
                .message("Customer Created Successfully")
                .build();
    }

    @Override
    public Response getCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(()-> new NotFoundException("Customer not found"));

        CustomerDTO customerDTO = modelMapper.map(customer,CustomerDTO.class);

        return Response.builder()
                .status(200)
                .message("Successful")
                .customer(customerDTO)
                .build();
    }

    @Override
    public Response getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();

        List<CustomerDTO> customerDTOS = modelMapper.map(customers, new TypeToken<List<CustomerDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Successful")
                .customers(customerDTOS)
                .build();
    }

    @Override
    public Response getAllCustomersPaginated(Integer page, Integer size, FilterEnum filter) {
        Pageable customerPages = PageRequest.of(page,size,getSortByFilter(filter));

        Page<Customer> customerPage = customerRepository.findByIsDeletedFalse(customerPages);

        List<Customer> customerList = customerPage.getContent();

        List<CustomerDTO> customerDTOList = modelMapper.map(customerList, new TypeToken<List<CustomerDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Suucess")
                .customers(customerDTOList)
                .build();
    }

    @Override
    public Response updateCustomer(Long CustomerId, CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(CustomerId)
                .orElseThrow(()-> new NotFoundException("Customer Not found"));

        if(customerDTO.getName() != null){
            customer.setName(customer.getName());
        }
        if(customerDTO.getAddress() != null){
            customer.setAddress(customer.getAddress());
        }
        if(customerDTO.getEmail() != null){
            customer.setEmail(customer.getEmail());
        }
        if(customerDTO.getContact_No() != null){
            customer.setContact_No(customer.getContact_No());
        }
        if(customerDTO.getPostalCode() != null){
            customer.setPostalCode(customer.getPostalCode());
        }
        if(customerDTO.getProvince() != null){
            customer.setProvince(customer.getProvince());
        }

        customerRepository.save(customer);

        return Response.builder()
                .status(200)
                .message("Customer Updated Successfully")
                .build();
    }

    @Override
    public Response deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(()-> new NotFoundException("Customer not found"));

        customerRepository.deleteById(customerId);

        return Response.builder()
                .status(204)
                .message("Customer deleted Successfully")
                .build();
    }

    @Override
    public Response softDeleteCustomer(Long customerId, Long userId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(()-> new NotFoundException("Customer not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("User not found"));

        customer.setDeletedBy(user);
        customer.setIsDeleted(true);

        customerRepository.save(customer);

        return Response.builder()
                .status(204)
                .message("Customer deleted Successfully")
                .build();
    }

    @Override
    public Response searchCustomer(String name) {
        List<Customer> customers = customerRepository.findByNameContaining(name);

        List<CustomerDTO> customerDTOList = modelMapper.map(customers, new TypeToken<List<CustomerDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Successful")
                .customers(customerDTOList)
                .build();
    }

    private Sort getSortByFilter(FilterEnum filter) {
        log.info(filter.toString());
        if (filter == FilterEnum.DESC) {
            return Sort.by(Sort.Direction.DESC, "name");
        } else {
            return Sort.by(Sort.Direction.ASC, "name");
        }
    }
}
