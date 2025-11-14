package com.example.hotel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.hotel.entity.Customer;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@SQLDelete(sql = "UPDATE bookings SET deleted = true WHERE id = ?") // 1. Ghi đè lệnh DELETE
@Where(clause = "deleted = false") // 2. Tự động lọc khi SELECT
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerFullName;

    @Column(nullable = false)
    private String customerPhone;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "price_per_night", nullable = false)
    private BigDecimal pricePerNight; // Giá mỗi đêm

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "booking_code", unique = true, nullable = false)
    private String bookingConfirmationCode;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "actual_checkin_time")
    private LocalDateTime actualCheckinTime;

    @Column(name = "actual_checkout_time")
    private LocalDateTime actualCheckoutTime;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "so_nguoi_lon")
    @ColumnDefault("1") // Đặt giá trị mặc định trong DB là 1
    private int soNguoiLon = 1; // Đặt giá trị mặc định trong Java là 1

    @Column(name = "so_tre_em")
    @ColumnDefault("0") // Đặt giá trị mặc định trong DB là 0
    private int soTreEm = 0; // Đặt giá trị mặc định trong Java là 0

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    public BigDecimal getAmountPaid() {
        if (this.payments == null || this.payments.isEmpty()) {
            return BigDecimal.ZERO;
        }
        // Tính tổng
        return this.payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}