package com.example.hotel.service;

import com.example.hotel.dto.PaymentDto;
import com.example.hotel.dto.PaymentRequestDto;
import java.util.List;

public interface PaymentService {
    PaymentDto recordPayment(PaymentRequestDto paymentDto);
    List<PaymentDto> getPaymentsForBooking(Long bookingId);
}