// src/main/java/com/example/hotel/dto/ProfileUpdateRequest.java
package com.example.hotel.dto;

import lombok.Data;

@Data // Tự động tạo Getters, Setters, toString...
public class ProfileUpdateRequest {
    private String fullName;
    private String about;
    private String company;
    private String job;
    private String country;
    private String address;
    private String phone;
    private String email; // Cân nhắc nếu cho phép đổi email
    private String twitter;
    private String facebook;
    private String instagram;
    private String linkedin;
}