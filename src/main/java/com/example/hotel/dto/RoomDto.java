package com.example.hotel.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

// DTO để nhận và gửi dữ liệu phòng
@Data
public class RoomDto {
    private Long id;
    private String roomNumber;
    private Long roomTypeId;
    private String roomTypeName;
    private String status;
    private Integer floor;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private String description;
    private List<String> amenities;

}
