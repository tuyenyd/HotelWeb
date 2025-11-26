package com.example.hotel.service;

import com.example.hotel.dto.EmployeeDto;

import java.util.List;

public interface EmployeeService {
    // Lấy danh sách tất cả nhân viên
    List<EmployeeDto> getAllEmployees();

    // Lấy thông tin chi tiết một nhân viên theo ID
    EmployeeDto getEmployeeById(Long id);

    // Tạo mới một nhân viên
    // Sử dụng EmployeeDto làm tham số đầu vào vì nó chứa đủ thông tin cần thiết
    EmployeeDto createEmployee(EmployeeDto dto);

    // Cập nhật thông tin nhân viên
    // ID là của nhân viên cần cập nhật, dto chứa thông tin mới
    EmployeeDto updateEmployee(Long id, EmployeeDto dto);

    // Xóa nhân viên theo ID
    void deleteEmployee(Long id);
}