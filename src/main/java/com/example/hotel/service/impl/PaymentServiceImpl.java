package com.example.hotel.service.impl;

import com.example.hotel.dto.PaymentDto;
import com.example.hotel.dto.PaymentRequestDto;
import com.example.hotel.entity.Booking;
import com.example.hotel.entity.Payment;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.repository.BookingRepository;
import com.example.hotel.repository.PaymentRepository;
import com.example.hotel.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Tự động @Autowired
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public PaymentDto recordPayment(PaymentRequestDto paymentDto) {
        // 1. Tìm đặt phòng
        Booking booking = bookingRepository.findById(paymentDto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + paymentDto.getBookingId()));

        // 2. Tạo đối tượng Payment mới
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(paymentDto.getAmount());
        payment.setMethod(paymentDto.getMethod());
        payment.setNotes(paymentDto.getNotes());
        payment.setPaymentDate(LocalDateTime.now());

        // 3. Lưu vào CSDL
        Payment savedPayment = paymentRepository.save(payment);

        // 4. Trả về DTO cho frontend
        return convertToDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsForBooking(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Booking not found: " + bookingId);
        }

        // Lấy danh sách từ CSDL
        List<Payment> payments = paymentRepository.findByBookingIdOrderByPaymentDateDesc(bookingId);

        // Chuyển đổi sang DTO
        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Hàm tiện ích
    private PaymentDto convertToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setMethod(payment.getMethod());
        dto.setNotes(payment.getNotes());
        dto.setPaymentDate(payment.getPaymentDate());
        return dto;
    }
}