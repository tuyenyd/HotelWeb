package com.example.hotel.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequestDto {
    private Long bookingId;
    private BigDecimal amount;
    private String method;
    private String notes;
}