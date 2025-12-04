package com.example.hotel.controller;

import com.example.hotel.entity.RoomType;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.repository.RoomTypeRepository;
import com.example.hotel.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/public/prices")
@RequiredArgsConstructor
public class PublicPriceController {

    private final PriceService priceService;
    private final RoomTypeRepository roomTypeRepository;

    // API tính tổng giá cho một khoảng thời gian đặt phòng
    @GetMapping("/calculate")
    public ResponseEntity<BigDecimal> calculateTotalPrice(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found"));

        BigDecimal totalPrice = priceService.calculateTotalPrice(roomType, checkIn, checkOut);
        return ResponseEntity.ok(totalPrice);
    }
}