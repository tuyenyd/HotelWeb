package com.example.hotel.controller; // Đặt trong package admin nếu bạn có

import com.example.hotel.dto.LoyaltyTierDto;
import com.example.hotel.service.LoyaltyTierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/loyalty-tiers")
@RequiredArgsConstructor
public class LoyaltyTierController {

    private final LoyaltyTierService loyaltyTierService;

    // API cho nút "Thêm hạng" (Modal)
    @PostMapping
    public ResponseEntity<LoyaltyTierDto> createTier(@RequestBody LoyaltyTierDto dto) {
        return ResponseEntity.ok(loyaltyTierService.createTier(dto));
    }

    // API để tải danh sách hạng
    @GetMapping
    public ResponseEntity<List<LoyaltyTierDto>> getAllTiers() {
        return ResponseEntity.ok(loyaltyTierService.getAllTiers());
    }

    // API cho nút "Sửa hạng" (Modal)
    @PutMapping("/{id}")
    public ResponseEntity<LoyaltyTierDto> updateTier(@PathVariable Long id, @RequestBody LoyaltyTierDto dto) {
        return ResponseEntity.ok(loyaltyTierService.updateTier(id, dto));
    }

    // API cho nút "Xóa"
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTier(@PathVariable Long id) {
        loyaltyTierService.deleteTier(id);
        return ResponseEntity.noContent().build();
    }
}