package com.example.hotel.dto; // (Hoặc package DTO của bạn)

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class CustomerRegisterRequest {
    @NotBlank
    @Size(min = 3, max = 150)
    private String fullName;

    @NotBlank
    @Size(min = 9, max = 20)
    private String idNumber; // CCCD

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    private String phone;

    private LocalDate dateOfBirth;

    private String address;
}