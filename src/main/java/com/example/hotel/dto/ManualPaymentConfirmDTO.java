package com.example.hotel.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ManualPaymentConfirmDTO {
    private Long bookingId;
    private BigDecimal amount;
}