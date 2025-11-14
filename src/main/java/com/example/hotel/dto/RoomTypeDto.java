package com.example.hotel.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomTypeDto {
    private Long id;
    private String name;
    private String typeCode;
    private String description;
    private Integer capacity;
    private BigDecimal basePrice;
    private Double area;
    private List<String> amenities;
    private int roomCount;
    private Integer pointsEarned;
}
