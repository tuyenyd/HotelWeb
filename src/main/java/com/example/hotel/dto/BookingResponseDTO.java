package com.example.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BookingResponseDTO {
    private String code; // Mã đặt phòng
    private String status;
    private BigDecimal total;
}