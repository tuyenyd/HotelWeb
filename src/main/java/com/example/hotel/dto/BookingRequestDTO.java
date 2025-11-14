package com.example.hotel.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingRequestDTO {

    // Từ Form 3: Chi tiết phòng
    private Long roomId;
    private String checkin;
    private String checkout;
    private int soNguoiLon;
    private int soTreEm;
    private BigDecimal total;
    private String notes; // (Lưu ý: Entity Booking của bạn chưa có trường này)

    // Từ Form 1: Thông tin khách vãng lai
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private String customerIdNumber; // (CCCD)
}