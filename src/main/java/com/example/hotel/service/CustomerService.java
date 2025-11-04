package com.example.hotel.service;

import com.example.hotel.dto.CustomerRequestDTO;
import com.example.hotel.dto.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {
    CustomerResponseDTO createCustomer(CustomerRequestDTO customerRequest);
    CustomerResponseDTO getCustomerById(Long customerId);
    List<CustomerResponseDTO> getAllCustomers();
    CustomerResponseDTO updateCustomer(Long customerId, CustomerRequestDTO customerRequest);
    void deleteCustomer(Long customerId);
}