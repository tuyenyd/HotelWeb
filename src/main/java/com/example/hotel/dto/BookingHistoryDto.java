package com.example.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryDto {
    private String bookingCode;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String roomTypeName;
    private String roomNumber;
    private BigDecimal totalPrice;
    private String status;
}