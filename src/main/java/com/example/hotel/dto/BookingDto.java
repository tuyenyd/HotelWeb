package com.example.hotel.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BookingDto {
    private Long id;
    private String code;
    private String created;
    private String status;
    private String customer;
    private String phone;
    private String roomType;
    private String roomNumber;
    private Long roomId;
    private long nights;
    private String checkin;
    private String checkout;
    private BigDecimal total;
    private Long customerId;
    private BigDecimal pricePerNight;
    private String actualCheckinTime;
    private String actualCheckoutTime;
    private int soNguoiLon;
    private int soTreEm;
    private BigDecimal amountPaid;
}
