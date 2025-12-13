package com.example.hotel.controller;

import com.example.hotel.dto.PriceCalculationResponse; // <-- Đảm bảo đã import DTO này
import com.example.hotel.service.CustomerService;
import com.example.hotel.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // <-- Import quan trọng cho Spring Security
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/public/prices")
@RequiredArgsConstructor
public class PublicPriceController {

    private final PriceService priceService;
    // Thêm service này để tìm ID khách hàng từ email
    private final CustomerService customerService;

    // Đã bỏ roomTypeRepository vì Service sẽ lo việc đó

    // API tính tổng giá chi tiết (có áp dụng giảm giá thành viên)
    @GetMapping("/calculate")
    public ResponseEntity<PriceCalculationResponse> calculateTotalPrice(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            // THÊM THAM SỐ NÀY ĐỂ NHẬN DIỆN NGƯỜI DÙNG
            Authentication authentication
    ) {

        Long customerId = null;

        // 1. Kiểm tra xem người dùng có đang đăng nhập không
        // "anonymousUser" là principal mặc định khi chưa đăng nhập
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {

            // Lấy email từ token
            String email = authentication.getName();

            // Tìm ID khách hàng (Giả sử bạn có hàm này trong CustomerService)
            // Nếu chưa có, bạn có thể dùng customerService.getCustomerProfileByEmail(email).getId()
            customerId = customerService.getCustomerIdByEmail(email);
        }

        // 2. Gọi hàm tính giá chi tiết ở Service (truyền thêm customerId)
        // Hàm này sẽ trả về object chứa: giá gốc, tiền giảm, giá cuối
        PriceCalculationResponse response = priceService.calculateDetailedPrice(roomTypeId, checkIn, checkOut, customerId);

        return ResponseEntity.ok(response);
    }
}