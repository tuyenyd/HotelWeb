package com.example.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyPointTransactionDto {
    private LocalDateTime createdAt;
    private Integer pointsEarned; // Có thể âm
    private String description;
}