package com.example.hotel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRequestDTO {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150, message = "Họ tên không quá 150 ký tự")
    private String fullName;

    @NotBlank(message = "Số CCCD/CMND không được để trống")
    @Size(max = 20, message = "Số CCCD/CMND không quá 20 ký tự")
    private String idNumber;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email không quá 255 ký tự")
    private String email;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dateOfBirth;

    private String phone;
    private String address;
}