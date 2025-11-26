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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
//import java.util.function.Predicate;
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
        payment.setTransactionReference(paymentDto.getTransactionReference());

        // 3. Lưu Payment vào CSDL
        Payment savedPayment = paymentRepository.save(payment);

        // 4. Cập nhật lại số tiền đã trả trong Booking
        BigDecimal currentPaid = booking.getAmountPaid(); // Lấy số tiền đã trả hiện tại
        if (currentPaid == null) {
            currentPaid = BigDecimal.ZERO;
        }

        // Cộng thêm số tiền vừa thanh toán
        booking.setAmountPaid(currentPaid.add(paymentDto.getAmount()));

        // Lưu Booking cập nhật vào CSDL
        bookingRepository.save(booking);

        // 5. Trả về DTO cho frontend
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

    @Override
    public List<PaymentDto> getAllPayments(LocalDate startDate, LocalDate endDate, String method) {
        // 1. Xây dựng các điều kiện lọc (Specification)
        Specification<Payment> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo ngày bắt đầu (từ đầu ngày)
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("paymentDate"), startDate.atStartOfDay()));
            }
            // Lọc theo ngày kết thúc (đến cuối ngày)
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("paymentDate"), endDate.atTime(23, 59, 59)));
            }
            // Lọc theo phương thức thanh toán (nếu có)
            if (method != null && !method.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("method"), method));
            }

            // Kết hợp tất cả các điều kiện bằng AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 2. Gọi repository với Specification và sắp xếp giảm dần theo ngày
        List<Payment> payments = paymentRepository.findAll();

        // 3. Convert sang DTO
        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDto getPaymentById(Long id) {
        // 1. Tìm payment trong DB theo ID
        com.example.hotel.entity.Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch thanh toán với ID: " + id));

        // 2. Chuyển đổi (map) từ Entity sang DTO
        // (Giả sử bạn đã có hàm convertToDto trong class này rồi)
        return convertToDto(payment);
    }
    // Hàm tiện ích
    // Hàm hỗ trợ chuyển đổi Entity -> DTO
    private PaymentDto convertToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setMethod(payment.getMethod());
        // Trong PaymentServiceImpl.java -> convertToDto()
        dto.setTransactionReference(payment.getTransactionReference()); // Đảm bảo entity có dữ liệu này
        dto.setNotes(payment.getNotes()); // Đảm bảo entity có dữ liệu này
        // dto.setStatus(payment.getStatus()); // Nếu có

        // 3. Lấy thông tin từ Booking liên quan để điền vào DTO
        if (payment.getBooking() != null) {
            dto.setBookingCode(payment.getBooking().getBookingConfirmationCode());
            dto.setCustomerName(payment.getBooking().getCustomerFullName());
        }

        // 4. Lấy mã giao dịch (nếu có lưu trong entity)
        // dto.setTransactionReference(payment.getTransactionReference());

        return dto;
    }

}