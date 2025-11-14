package com.example.hotel.controller;

import com.example.hotel.dto.BookingRequestDTO;
import com.example.hotel.dto.BookingResponseDTO;
import com.example.hotel.dto.RoomDto;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.RoomService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public") // Đường dẫn công khai
@RequiredArgsConstructor
@Slf4j
public class PublicBookingController {

    private final RoomService roomService;
    private final BookingService bookingService;

    /**
     * API CÔNG KHAI: Tìm phòng còn trống
     * GET /api/public/rooms/available?checkin=...&checkout=...&adults=...&children=...
     */
    @GetMapping("/rooms/available")
    public ResponseEntity<List<RoomDto>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout,
            @RequestParam("adults") int adults,
            @RequestParam("children") int children) {

        log.info("Nhận yêu cầu tìm phòng: {} đến {}, {} người lớn, {} trẻ em", checkin, checkout, adults, children);
        List<RoomDto> availableRooms = roomService.findAvailableRooms(checkin, checkout, adults + children);
        return ResponseEntity.ok(availableRooms);
    }

    /**
     * API CÔNG KHAI: Tạo một đặt phòng mới
     * POST /api/public/bookings
     */
    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(
            @RequestBody BookingRequestDTO bookingRequest,
            @RequestHeader(value = "Authorization", required = false) String token) {

        log.info("Nhận yêu cầu đặt phòng mới cho khách: {}", bookingRequest.getCustomerEmail());
        try {
            // Service sẽ xử lý logic (có token hoặc không)
            BookingResponseDTO newBooking = bookingService.createPublicBooking(bookingRequest, token);
            return ResponseEntity.ok(newBooking);

        } catch (RuntimeException e) {
            // Trả về lỗi nếu phòng không còn trống hoặc dữ liệu sai
            log.error("Lỗi khi tạo đặt phòng: {}", e.getMessage());
            // Trả về một đối tượng JSON chuẩn (giống như trang login)
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @Data
    @AllArgsConstructor
    static class MessageResponse {
        private String message;
    }
}