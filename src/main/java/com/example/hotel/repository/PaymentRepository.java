package com.example.hotel.repository;

import com.example.hotel.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Tìm tất cả thanh toán của một Booking,
     * sắp xếp theo ngày thanh toán mới nhất trước.
     */
    List<Payment> findByBookingIdOrderByPaymentDateDesc(Long bookingId);
}