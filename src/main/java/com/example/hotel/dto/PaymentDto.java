package com.example.hotel.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDto {
    private Long id;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private String method;
    private String notes;
}