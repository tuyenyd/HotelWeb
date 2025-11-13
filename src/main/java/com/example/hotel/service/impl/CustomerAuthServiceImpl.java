package com.example.hotel.service.impl;

import com.example.hotel.dto.CustomerRegisterRequest;
import com.example.hotel.entity.Customer;
import com.example.hotel.entity.LoyaltyTier;
import com.example.hotel.repository.CustomerRepository;
import com.example.hotel.repository.LoyaltyTierRepository;
import com.example.hotel.service.CustomerAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerAuthServiceImpl implements CustomerAuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoyaltyTierRepository loyaltyTierRepository;

    @Override
    public Customer registerCustomer(CustomerRegisterRequest registerRequest) {
        if (customerRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Lỗi: Email đã được sử dụng!");
        }

        if (customerRepository.findByIdNumber(registerRequest.getIdNumber()).isPresent()) {
            throw new RuntimeException("Lỗi: Số CCCD/CMND đã tồn tại!");
        }

        Customer customer = new Customer();
        customer.setFullName(registerRequest.getFullName());
        customer.setIdNumber(registerRequest.getIdNumber());
        customer.setEmail(registerRequest.getEmail());
        customer.setPhone(registerRequest.getPhone());
        customer.setDateOfBirth(registerRequest.getDateOfBirth());
        customer.setAddress(registerRequest.getAddress());

        // Mã hóa mật khẩu
        customer.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Gán hạng thành viên mặc định
        LoyaltyTier defaultTier = loyaltyTierRepository.findByName("Bronze")
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy hạng Đồng."));
        customer.setLoyaltyTier(defaultTier);
        customer.setCurrentPoints(0);

        return customerRepository.save(customer);
    }
}