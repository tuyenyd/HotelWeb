package com.example.hotel.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomTypeDto implements Serializable {
    private static final long serialVersionUID = 1L;
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
    private String imageUrl;
    private String galleryImages;
    private String overview;
}
