package com.example.hotel.controller;

import com.example.hotel.dto.BookingHistoryDto;
import com.example.hotel.dto.CustomerRequestDTO;
import com.example.hotel.dto.CustomerResponseDTO;
import com.example.hotel.dto.LoyaltyPointTransactionDto;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.CustomerService;
import com.example.hotel.service.LoyaltyAutomationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class CustomerAdminController {

    private final CustomerService customerService;
    private final BookingService bookingService;
    private final LoyaltyAutomationService loyaltyService;

    // Tạo khách hàng mới
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(
            @Valid @RequestBody CustomerRequestDTO customerRequest) {
        CustomerResponseDTO newCustomer = customerService.createCustomer(customerRequest);
        return new ResponseEntity<>(newCustomer, HttpStatus.CREATED);
    }

    // Lấy tất cả khách hàng
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        List<CustomerResponseDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    // Lấy 1 khách hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        CustomerResponseDTO customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    // Cập nhật khách hàng
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDTO customerRequest) {
        CustomerResponseDTO updatedCustomer = customerService.updateCustomer(id, customerRequest);
        return ResponseEntity.ok(updatedCustomer);
    }

    // Xóa khách hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
    // LỊCH SỬ ĐẶT PHÒNG
    @GetMapping("/{customerId}/bookings")
    public ResponseEntity<List<BookingHistoryDto>> getBookingHistory(@PathVariable Long customerId) {
        List<BookingHistoryDto> history = bookingService.getBookingHistoryByCustomerId(customerId);
        return ResponseEntity.ok(history);
    }


    // LỊCH SỬ ĐIỂM
    @GetMapping("/{customerId}/points-history")
    public ResponseEntity<List<LoyaltyPointTransactionDto>> getPointsHistory(@PathVariable Long customerId) {
        List<LoyaltyPointTransactionDto> history = loyaltyService.getPointsHistoryByCustomerId(customerId);
        return ResponseEntity.ok(history);
    }

}