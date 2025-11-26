package com.example.hotel.service.impl;

import com.example.hotel.dto.EmployeeDto;
import com.example.hotel.entity.Employee;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.repository.EmployeeRepository;
import com.example.hotel.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Cacheable(value = "employees", key = "'all'")
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "employee", key = "#id")
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên ID: " + id));
        return convertToDto(employee);
    }

    @Override
    @Transactional
    @CacheEvict(value = "employees", key = "'all'")
    public EmployeeDto createEmployee(EmployeeDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }
        Employee employee = new Employee();
        // Map dữ liệu từ DTO sang Entity
        mapDtoToEntity(dto, employee);

        Employee savedEmployee = employeeRepository.save(employee);
        return convertToDto(savedEmployee);
    }

    @Override
    @Transactional
    @Caching(
            put = { @CachePut(value = "employee", key = "#id") },
            evict = { @CacheEvict(value = "employees", key = "'all'") }
    )
    public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên ID: " + id));

        // Cập nhật dữ liệu (có thể thêm kiểm tra email nếu email thay đổi)
        mapDtoToEntity(dto, employee);

        Employee updatedEmployee = employeeRepository.save(employee);
        return convertToDto(updatedEmployee);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "employee", key = "#id"),
                    @CacheEvict(value = "employees", key = "'all'")
            }
    )
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên ID: " + id);
        }
        employeeRepository.deleteById(id);
    }

    // Hàm tiện ích
    private EmployeeDto convertToDto(Employee entity) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(entity.getId());
        dto.setEmployeeCode(entity.getEmployeeCode());
        dto.setFullName(entity.getFullName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setPosition(entity.getPosition());
        dto.setDepartment(entity.getDepartment());
        dto.setJoinDate(entity.getJoinDate());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    private void mapDtoToEntity(EmployeeDto dto, Employee entity) {
        entity.setFullName(dto.getFullName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setPosition(dto.getPosition());
        entity.setDepartment(dto.getDepartment());
        if (dto.getJoinDate() != null) entity.setJoinDate(dto.getJoinDate());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
    }
}