package com.example.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "employees") // Bảng này giờ đứng độc lập
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String employeeCode; // Mã NV (Vd: NV001)

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String position; // Chức vụ
    private String department; // Phòng ban

    private LocalDate joinDate; // Ngày vào làm
    private String status; // Trạng thái làm việc

    // Tự động tạo dữ liệu khi thêm mới
    @PrePersist
    protected void onCreate() {
        if (joinDate == null) joinDate = LocalDate.now();
        if (status == null) status = "Đang làm việc";
        // Tạo mã NV tự động đơn giản (thực tế nên dùng cách khác xịn hơn)
        if (employeeCode == null) this.employeeCode = "NV" + System.currentTimeMillis() % 10000;
    }
}