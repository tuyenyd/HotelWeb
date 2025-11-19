package com.example.hotel.exception; // (Bạn có thể cần tạo package "exception" này)

import com.example.hotel.dto.MessageResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt lỗi ràng buộc khóa ngoại (lỗi 1451 của MySQL)
     * Lỗi này xảy ra khi cố xóa một "cha" (Phòng, Khách hàng)
     * mà vẫn còn "con" (Đặt phòng) trỏ đến nó.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MessageResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMessage();

        // Tùy chỉnh thông báo lỗi dựa trên nội dung
        if (message.contains("FOREIGN KEY (`room_id`)")) {
            return new ResponseEntity<>(
                    new MessageResponse("Không thể xóa. Đã có đặt phòng liên quan đến phòng này."),
                    HttpStatus.BAD_REQUEST); // Trả về lỗi 400
        }

        if (message.contains("FOREIGN KEY (`customer_id`)")) {
            return new ResponseEntity<>(
                    new MessageResponse("Không thể xóa. Đã có đặt phòng liên quan đến khách hàng này."),
                    HttpStatus.BAD_REQUEST); // Trả về lỗi 400
        }

        // Lỗi chung (ví dụ: trùng email, trùng số phòng)
        return new ResponseEntity<>(
                new MessageResponse("Lỗi ràng buộc dữ liệu. Dữ liệu có thể đang được sử dụng hoặc bị trùng lặp."),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Bắt các lỗi RuntimeException chung (giống như các lỗi chúng ta tự ném ra)
     * Ví dụ: "Mật khẩu hiện tại không đúng"
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponse> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(
                new MessageResponse(ex.getMessage()),
                HttpStatus.BAD_REQUEST); // Trả về lỗi 400
    }
}