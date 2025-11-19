package com.example.hotel.controller;

import com.example.hotel.dto.*;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.CustomerService;
import com.example.hotel.service.VnPayService;
import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Dùng để lấy người dùng đã đăng nhập
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public/customer") // API được bảo vệ cho khách hàng
@RequiredArgsConstructor
@Slf4j
public class CustomerPublicController {

    private final CustomerService customerService;
    private final BookingService bookingService;
    private final VnPayService vnPayService;

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
    /**
     * API: Lấy chi tiết một đặt phòng (bao gồm cả thanh toán)
     * (Dùng cho modal trong trang user-bookings.html)
     * GET /api/public/customer/bookings/{id}
     */
    @GetMapping("/bookings/{id}")
    public ResponseEntity<?> getBookingDetails(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Khách hàng {} đang lấy chi tiết booking ID: {}", authentication.getName(), id);
        try {
            // Lấy email khách hàng từ token
            String email = authentication.getName();

            // Gọi service để lấy chi tiết (Service sẽ kiểm tra quyền sở hữu)
            CustomerBookingDetailDTO bookingDetail = bookingService.getBookingDetailsForCustomer(email, id);

            return ResponseEntity.ok(bookingDetail);

        } catch (EntityNotFoundException e) {
            log.warn("Không tìm thấy booking: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("Lỗi truy cập: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi không xác định khi lấy chi tiết booking: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Lỗi máy chủ"));
        }
    }
    @PostMapping("/create-payment-url")
    public ResponseEntity<?> createPaymentUrl(
            Authentication authentication,
            @RequestBody PaymentRequestDto request) {

        log.info("Khách hàng {} yêu cầu tạo link thanh toán cho Booking ID: {}",
                authentication.getName(), request.getBookingId());
        try {

            // Gọi service (giả lập) để tạo link
            String paymentUrl = vnPayService.createPaymentUrl(request.getBookingId(), request.getAmount());

            return ResponseEntity.ok(new PaymentUrlResponseDTO(paymentUrl));

        } catch (Exception e) {
            log.error("Lỗi khi tạo link thanh toán: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    @PostMapping("/payment-success")
    public ResponseEntity<?> confirmPaymentSuccess(
            Authentication authentication,
            @RequestBody PaymentSuccessRequest request) { // Chúng ta sẽ tạo DTO này bên dưới

        try {
            bookingService.processPaymentSuccess(request.getBookingId(), request.getAmount(), request.getTxnRef());
            return ResponseEntity.ok(new MessageResponse("Thanh toán được ghi nhận thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // DTO tĩnh (hoặc tạo file riêng)
    @Data
    public static class PaymentSuccessRequest {
        private Long bookingId;
        private BigDecimal amount;
        private String txnRef;
    }
    @PatchMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, Authentication authentication) {
        try {
            bookingService.cancelBookingByCustomer(authentication.getName(), id);
            return ResponseEntity.ok(new MessageResponse("Hủy đặt phòng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}