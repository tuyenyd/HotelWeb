package com.example.hotel.controller;

import com.example.hotel.dto.CustomerLoginRequest;
import com.example.hotel.dto.CustomerRegisterRequest;
import com.example.hotel.dto.JwtResponse;
import com.example.hotel.dto.MessageResponse;
import com.example.hotel.entity.Customer;
import com.example.hotel.repository.CustomerRepository;
import com.example.hotel.security.jwt.JwtUtils;
import com.example.hotel.service.CustomerAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public/auth") // Endpoint công khai
@RequiredArgsConstructor
public class AuthUserController {

    private final CustomerRepository customerRepository;
    private final CustomerAuthService customerAuthService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils; // Sử dụng lại JwtUtils của admin

    /**
     * API Đăng nhập cho Khách hàng
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateCustomer(@Valid @RequestBody CustomerLoginRequest loginRequest) {

        try {
            // Tìm khách hàng bằng email
            Customer customer = customerRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng!"));

            // Kiểm tra mật khẩu
            if (customer.getPassword() == null || !passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
                throw new RuntimeException("Email hoặc mật khẩu không đúng!");
            }

            // Tạo JWT Token (chỉ chứa email của khách hàng)
            String jwt = jwtUtils.generateTokenFromUsername(customer.getEmail());

            // Trả về token (Thành công)
            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    customer.getId(),
                    customer.getEmail(),
                    customer.getFullName(),
                    customer.getPhone(),
                    customer.getAddress(),
                    customer.getIdNumber()

            ));

        } catch (RuntimeException e) {
            // "Bắt" lỗi "Email hoặc mật khẩu không đúng!" và trả về JSON
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * API Đăng ký cho Khách hàng
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody CustomerRegisterRequest registerRequest) {
        try {
            customerAuthService.registerCustomer(registerRequest);
            return ResponseEntity.ok(new MessageResponse("Đăng ký khách hàng thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}