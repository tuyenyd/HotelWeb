package com.example.hotel.controller;

import com.example.hotel.dto.SePayWebhookDTO;
import com.example.hotel.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/webhook/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final BookingService bookingService;

    // Regex MỚI: Tìm chuỗi BK-XXXXXXXX nằm Ở BẤT KỲ ĐÂU trong nội dung
    // Không dùng dấu ^ và $ nữa
    private static final Pattern BOOKING_CODE_PATTERN = Pattern.compile("BK-?[A-Z0-9]{8,12}");

    @PostMapping("/sepay-confirm")
    public ResponseEntity<String> receiveSePayNotification(@RequestBody SePayWebhookDTO payload) {
        log.info("=== NHẬN ĐƯỢC WEBHOOK TỪ SEPAY ===");
        log.info("Dữ liệu payload: {}", payload);

        try {
            String content = payload.getContent();
            BigDecimal amount = payload.getAmount();
            String txnId = payload.getId().toString();

            // 1. Kiểm tra dữ liệu quan trọng (SỬA LỖI amount=null)
            if (content == null || content.trim().isEmpty()) {
                log.warn("Webhook bị bỏ qua: Nội dung chuyển khoản (content) bị trống.");
                return ResponseEntity.ok("Ignored: Content is empty");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Webhook bị bỏ qua: Số tiền (amount) không hợp lệ: {}", amount);
                // Vẫn trả về 200 OK để SePay không gửi lại request lỗi này
                return ResponseEntity.ok("Ignored: Invalid amount");
            }

            // 2. Trích xuất mã booking từ nội dung (DÙNG REGEX MỚI)
            String bookingCode = extractBookingCode(content);

            if (bookingCode == null) {
                log.warn("Không tìm thấy mã booking hợp lệ (dạng BK-XXXXXXXX) trong nội dung hỗn tạp: {}", content);
                return ResponseEntity.ok("Received but no valid booking code found");
            }

            log.info(">>> ĐÃ TRÍCH XUẤT THÀNH CÔNG Mã Booking: {}, Số tiền: {}", bookingCode, amount);

            // 3. Gọi Service xử lý nghiệp vụ
            // Lưu ý: Nếu txnId là null (do test), ta dùng một giá trị tạm
            String finalTxnId = (txnId != null) ? txnId : "TEST_TXN_" + System.currentTimeMillis();

            bookingService.processRealtimePayment(bookingCode, amount, finalTxnId);

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Lỗi NGUY HIỂM khi xử lý webhook SePay: {}", e.getMessage(), e);
            // Trả về 500 để SePay biết server mình đang gặp sự cố
            return ResponseEntity.internalServerError().body("Server Error: " + e.getMessage());
        }
    }

    // Hàm helper dùng Regex để trích xuất mã
    private String extractBookingCode(String content) {
        if (content == null) return null;
        String upperContent = content.toUpperCase();

        // Sử dụng matcher.find() để tìm kiếm chuỗi con khớp với pattern
        Matcher matcher = BOOKING_CODE_PATTERN.matcher(upperContent);
        if (matcher.find()) {
            String rawCode = matcher.group(); // Ví dụ nhận được: "BKD6E7D89A" hoặc "BK-D6E7D89A"

            // --- CHUẨN HÓA MÃ ---
            // Nếu mã tìm thấy không có dấu gạch ngang "-", ta tự động thêm vào sau chữ "BK"
            // để đảm bảo khớp với định dạng trong Database (BK-XXXXXXXX)
            if (!rawCode.contains("-") && rawCode.length() > 2) {
                return "BK-" + rawCode.substring(2);
            }

            // Nếu đã có dấu gạch ngang thì trả về nguyên vẹn
            return rawCode;
        }
        return null;
    }
}