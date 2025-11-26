package com.example.hotel.service;

import com.example.hotel.dto.PaymentDto;
import com.example.hotel.dto.PaymentRequestDto;
import com.example.hotel.entity.Booking;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;

public interface PaymentService {
    PaymentDto recordPayment(PaymentRequestDto paymentDto);
    List<PaymentDto> getPaymentsForBooking(Long bookingId);
    List<PaymentDto> getAllPayments(LocalDate startDate, LocalDate endDate, String method);
    // Trong PaymentService.java
    PaymentDto getPaymentById(Long id);

}