package com.example.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomerResponseDTO {
    private Long id;
    private String fullName;
    private String idNumber;
    private String email;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private Integer currentPoints;
    private Long loyaltyTierId;
    private String loyaltyTierName;
    private String loyaltyBenefitsJson;
    private Integer bookings; // Số lần đặt phòng (CHECKED_OUT)
    private Long nights;      // Tổng số đêm đã ở
    private BigDecimal spend; // Tổng số tiền đã chi tiêu
}