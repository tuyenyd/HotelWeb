package com.example.hotel.dto;

public class JwtResponse {
    private String token;
    private String type = "Bearer";

    // ⭐ SỬA: Thêm các trường thông tin người dùng
    private String username;
    private String fullName;
    private String role;
    private String avatarUrl;

    public JwtResponse(String accessToken, String username, String fullName, String role, String avatarUrl) {
        this.token = accessToken;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.avatarUrl = avatarUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String role) {
        this.avatarUrl = avatarUrl;
    }
}
