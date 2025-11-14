package com.example.hotel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking; // Liên kết tới Đặt phòng

    @Column(nullable = false)
    private BigDecimal amount; // Số tiền

    @Column(nullable = false)
    private String method; // Phương thức: CASH, BANK_TRANSFER

    @Column(columnDefinition = "TEXT")
    private String notes; // Ghi chú

    @Column(nullable = false)
    private LocalDateTime paymentDate; // Ngày thanh toán
}