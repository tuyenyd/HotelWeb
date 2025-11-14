package com.example.hotel.dto; // (Hoặc package DTO của bạn)

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CustomerLoginRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}