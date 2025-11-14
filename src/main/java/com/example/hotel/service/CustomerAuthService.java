package com.example.hotel.service;

import com.example.hotel.dto.CustomerRegisterRequest;
import com.example.hotel.entity.Customer;

public interface CustomerAuthService {
    Customer registerCustomer(CustomerRegisterRequest registerRequest);
}