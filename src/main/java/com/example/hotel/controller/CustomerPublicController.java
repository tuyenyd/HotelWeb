package com.example.hotel.controller;

import com.example.hotel.dto.BookingHistoryDto;
import com.example.hotel.dto.CustomerRequestDTO; // Dùng lại DTO này
import com.example.hotel.dto.CustomerResponseDTO;
import com.example.hotel.dto.PasswordChangeRequest;
import com.example.hotel.dto.MessageResponse; // Dùng lại DTO này
import com.example.hotel.service.BookingService;
import com.example.hotel.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Dùng để lấy người dùng đã đăng nhập
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public/customer") // API được bảo vệ cho khách hàng
@RequiredArgsConstructor
@Slf4j
public class CustomerPublicController {

    private final CustomerService customerService;
    private final BookingService bookingService;

    /**
     * API: Lấy thông tin hồ sơ của khách hàng hiện tại
     * (Dùng cho trang user-profile.html)
     * GET /api/public/customer/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<CustomerResponseDTO> getCustomerProfile(Authentication authentication) {
        // authentication.getName() sẽ là email của khách hàng (đã được xác thực từ token)
        log.info("Đang lấy hồ sơ cho khách hàng: {}", authentication.getName());
        CustomerResponseDTO customerDto = customerService.getCustomerProfileByEmail(authentication.getName());
        return ResponseEntity.ok(customerDto);
    }

    /**
     * API: Cập nhật thông tin hồ sơ
     * (Dùng cho trang user-profile.html)
     * PUT /api/public/customer/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateCustomerProfile(Authentication authentication, @RequestBody CustomerRequestDTO customerRequest) {
        log.info("Đang cập nhật hồ sơ cho khách hàng: {}", authentication.getName());
        try {
            CustomerResponseDTO updatedDto = customerService.updateCustomerProfile(authentication.getName(), customerRequest);
            return ResponseEntity.ok(updatedDto);
        } catch (RuntimeException e) {
            log.error("Lỗi cập nhật hồ sơ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * API: Đổi mật khẩu
     * (Dùng cho trang user-profile.html)
     * PATCH /api/public/customer/change-password
     */
    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody PasswordChangeRequest request) {
        log.info("Đang đổi mật khẩu cho khách hàng: {}", authentication.getName());
        try {
            customerService.changeCustomerPassword(authentication.getName(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(new MessageResponse("Đổi mật khẩu thành công!"));
        } catch (RuntimeException e) {
            log.error("Lỗi đổi mật khẩu: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * API: Lấy lịch sử đặt phòng
     * (Dùng cho trang user-bookings.html)
     * GET /api/public/customer/bookings
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingHistoryDto>> getCustomerBookings(Authentication authentication) {
        log.info("Đang lấy lịch sử đặt phòng cho khách hàng: {}", authentication.getName());
        // 1. Lấy email từ token
        String email = authentication.getName();

        // 2. Dùng email tìm Customer ID
        Long customerId = customerService.getCustomerIdByEmail(email);

        // 3. Gọi service BookingService (bạn đã có sẵn hàm này)
        List<BookingHistoryDto> history = bookingService.getBookingHistoryByCustomerId(customerId);
        return ResponseEntity.ok(history);
    }
}