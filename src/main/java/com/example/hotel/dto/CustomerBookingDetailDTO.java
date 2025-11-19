package com.example.hotel.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CustomerBookingDetailDTO {
    // Thông tin cơ bản
    private Long id;
    private String bookingCode;
    private String status;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int soNguoiLon;
    private int soTreEm;

    // Thông tin phòng
    private String roomNumber;
    private String roomTypeName;

    // Thông tin tài chính
    private BigDecimal totalPrice;
    private BigDecimal amountPaid; // (Sẽ được tính toán)
    private List<PaymentDto> payments; // (Lịch sử giao dịch)
}