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
    /**
     * Lấy hồ sơ khách hàng (CustomerResponseDTO) bằng email (từ token).
     */
    CustomerResponseDTO getCustomerProfileByEmail(String email);

    /**
     * Lấy ID khách hàng (Long) bằng email (từ token).
     * Dùng để gọi getBookingHistoryByCustomerId
     */
    Long getCustomerIdByEmail(String email);

    /**
     * Cập nhật hồ sơ (chỉ các trường được phép) bằng email (từ token).
     */
    CustomerResponseDTO updateCustomerProfile(String email, CustomerRequestDTO customerRequest);

    /**
     * Đổi mật khẩu cho khách hàng bằng email (từ token).
     */
    void changeCustomerPassword(String email, String currentPassword, String newPassword);
}