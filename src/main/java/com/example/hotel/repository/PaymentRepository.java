package com.example.hotel.repository;

import com.example.hotel.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 1. Thêm import này
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// 2. Thêm ", JpaSpecificationExecutor<Payment>" vào cuối dòng này
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    // Các phương thức khác giữ nguyên...
    List<Payment> findByBookingIdOrderByPaymentDateDesc(Long bookingId);
    //List<Payment> findAllByOrderByPaymentDateDesc();
}