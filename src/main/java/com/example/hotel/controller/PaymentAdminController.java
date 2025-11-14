package com.example.hotel.controller;

import com.example.hotel.dto.PaymentDto;
import com.example.hotel.dto.PaymentRequestDto;
import com.example.hotel.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentAdminController {

    private final PaymentService paymentService;

    @PostMapping("/record")
    public ResponseEntity<PaymentDto> recordPayment(@RequestBody PaymentRequestDto paymentDto) {
        PaymentDto newPayment = paymentService.recordPayment(paymentDto);
        return new ResponseEntity<>(newPayment, HttpStatus.CREATED);
    }
}