package com.example.hotel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class EmployeeDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id; // Có thể null khi tạo mới
    private String employeeCode; // Backend tự tạo
    private String fullName;
    private String email;
    private String phone;
    private String position;
    private String department;
    private LocalDate joinDate;
    private String status;
}