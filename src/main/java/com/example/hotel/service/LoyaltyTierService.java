package com.example.hotel.service;

import com.example.hotel.dto.LoyaltyTierDto;
import com.example.hotel.entity.LoyaltyTier;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.repository.LoyaltyTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoyaltyTierService {

    private final LoyaltyTierRepository loyaltyTierRepository;

    // Lấy tất cả các hạng để hiển thị
    public List<LoyaltyTierDto> getAllTiers() {
        return loyaltyTierRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Tạo hạng mới (từ Modal)
    public LoyaltyTierDto createTier(LoyaltyTierDto dto) {
        LoyaltyTier tier = convertToEntity(dto);
        LoyaltyTier savedTier = loyaltyTierRepository.save(tier);
        return convertToDto(savedTier);
    }

    // Cập nhật hạng (từ Modal)
    public LoyaltyTierDto updateTier(Long id, LoyaltyTierDto dto) {
        LoyaltyTier tier = loyaltyTierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hạng với ID: " + id));

        tier.setName(dto.getName());
        tier.setPointsRequired(dto.getPointsRequired());
        tier.setDescription(dto.getDescription());
        tier.setBenefitsJson(dto.getBenefitsJson());

        LoyaltyTier updatedTier = loyaltyTierRepository.save(tier);
        return convertToDto(updatedTier);
    }

    // Xóa hạng
    public void deleteTier(Long id) {
        if (!loyaltyTierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy hạng với ID: " + id);
        }
        // Cần cẩn thận: Nếu có khách hàng đang ở hạng này, việc xóa sẽ gây lỗi.
        // Bạn nên thêm logic kiểm tra trước khi xóa.
        loyaltyTierRepository.deleteById(id);
    }

    // --- Hàm chuyển đổi ---
    private LoyaltyTierDto convertToDto(LoyaltyTier tier) {
        LoyaltyTierDto dto = new LoyaltyTierDto();
        dto.setId(tier.getId());
        dto.setName(tier.getName());
        dto.setPointsRequired(tier.getPointsRequired());
        dto.setDescription(tier.getDescription());
        dto.setBenefitsJson(tier.getBenefitsJson());
        return dto;
    }

    private LoyaltyTier convertToEntity(LoyaltyTierDto dto) {
        LoyaltyTier tier = new LoyaltyTier();
        // Không set ID khi tạo mới
        tier.setName(dto.getName());
        tier.setPointsRequired(dto.getPointsRequired());
        tier.setDescription(dto.getDescription());
        tier.setBenefitsJson(dto.getBenefitsJson());
        return tier;
    }
}