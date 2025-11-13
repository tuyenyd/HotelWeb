package com.example.hotel.dto;

import lombok.Getter;
import lombok.Setter;
// Chúng ta sẽ không dùng @AllArgsConstructor vì có 2 constructor khác nhau

@Setter
@Getter
public class JwtResponse {

    private String token;
    private String type = "Bearer";

    private String username; // Tên đăng nhập (cho Admin)
    private String role;
    private String avatarUrl;

    // --- Bổ sung các trường cho Customer ---
    private Long id; // ID của khách hàng
    private String fullName;
    private String email;    // Dùng trường email riêng
    private String phone;
    private String address;
    private String idNumber;

    // Constructor 1: Dành cho Admin
    public JwtResponse(String accessToken, String username, String fullName, String role, String avatarUrl) {
        this.token = accessToken;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.avatarUrl = avatarUrl;
    }

    // Constructor 2: Dành cho Customer
    public JwtResponse(String token, Long id, String email, String fullName, String phone, String address, String idNumber) {
        this.token = token;
        this.id = id;
        this.email = email; // Đặt email
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.idNumber = idNumber;
    }

}