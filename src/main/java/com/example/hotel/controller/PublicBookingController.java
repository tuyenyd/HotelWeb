package com.example.hotel.controller;

import com.example.hotel.dto.*;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.RoomService;
import com.example.hotel.service.RoomTypeService;
import jakarta.persistence.EntityNotFoundException;
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
    private final RoomTypeService roomTypeService;

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
    @GetMapping("/rooms")
    public ResponseEntity<List<RoomDto>> getAllPublicRooms() {
        // Dùng lại hàm getAllRooms() của admin
        List<RoomDto> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }
    @GetMapping("/room-types")
    public ResponseEntity<List<RoomTypeDto>> getAllRoomTypes() {
        // Giả sử RoomTypeService của bạn có hàm getAllRoomTypes()
        List<RoomTypeDto> roomTypes = roomTypeService.getAllRoomTypes();
        return ResponseEntity.ok(roomTypes);
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<RoomDto> getPublicRoomById(@PathVariable Long id) {
        // Dùng lại hàm getRoomById của admin
        RoomDto room = roomService.getRoomById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phòng với ID: " + id));
        return ResponseEntity.ok(room);
    }
    @GetMapping("/room-types/{id}")
    public ResponseEntity<RoomTypeDto> getRoomTypeById(@PathVariable Long id) {
        RoomTypeDto roomType = roomTypeService.getRoomTypeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng ID: " + id));
        return ResponseEntity.ok(roomType);
    }

}