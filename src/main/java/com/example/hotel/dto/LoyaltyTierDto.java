package com.example.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class LoyaltyTierDto {
    private Long id; // Chỉ dùng cho Response
    private String name;
    private Integer pointsRequired;
    private String description;
    private String benefitsJson; // Nhận và gửi String JSON
    private BigDecimal discountPercentage;
}