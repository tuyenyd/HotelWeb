// src/main/java/com/example/hotel/dto/PasswordChangeRequest.java
package com.example.hotel.dto;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    private String currentPassword;
    private String newPassword;
}