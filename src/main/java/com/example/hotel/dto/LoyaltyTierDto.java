package com.example.hotel.dto;

import lombok.Data;


@Data
public class LoyaltyTierDto {
    private Long id; // Chỉ dùng cho Response
    private String name;
    private Integer pointsRequired;
    private String description;
    private String benefitsJson; // Nhận và gửi String JSON
}