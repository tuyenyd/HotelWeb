package com.example.hotel.repository;

import com.example.hotel.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Kiểm tra trùng lặp
    boolean existsByEmail(String email);
}