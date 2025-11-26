package com.example.hotel.controller;

import com.example.hotel.dto.PaymentDto;
import com.example.hotel.dto.PaymentRequestDto;
import com.example.hotel.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
    @GetMapping
    public ResponseEntity<List<PaymentDto>> getAllPayments(
            // Nhận tham số lọc từ URL (ví dụ: ?startDate=2023-10-01&method=CASH)
            // required = false nghĩa là nếu không truyền thì giá trị sẽ là null
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String method
    ) {
        // Gọi service với các tham số lọc
        List<PaymentDto> payments = paymentService.getAllPayments(startDate, endDate, method);
        return ResponseEntity.ok(payments);
    }
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long id) {
        // Gọi service để lấy chi tiết
        // Bạn CẦN đảm bảo Interface PaymentService và Class PaymentServiceImpl
        // đã có phương thức getPaymentById(Long id) trả về PaymentDto.
        PaymentDto paymentDto = paymentService.getPaymentById(id);

        return ResponseEntity.ok(paymentDto);
    }
}