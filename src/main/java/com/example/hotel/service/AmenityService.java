package com.example.hotel.service;

import com.example.hotel.dto.AmenityRequestDTO;
import com.example.hotel.dto.AmenityResponseDTO;
import com.example.hotel.entity.Amenity;
import com.example.hotel.repository.AmenityRepository;
import com.example.hotel.repository.RoomTypeAmenityRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class AmenityService {

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private RoomTypeAmenityRepository roomTypeAmenityRepository;

    // ===================== MAPPER =====================
    private Amenity mapToEntity(AmenityRequestDTO dto) {
        Amenity entity = new Amenity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setIconClass(dto.getIconClass());
        entity.setCategory(dto.getType());
        entity.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        entity.setIsChargeable(dto.getIsChargeable() != null ? dto.getIsChargeable() : false);
        entity.setImageUrl(dto.getImageUrl());
        return entity;
    }

    private AmenityResponseDTO mapToResponseDto(Amenity entity) {
        AmenityResponseDTO dto = new AmenityResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setIconClass(entity.getIconClass());
        dto.setType(entity.getCategory());
        dto.setPrice(entity.getPrice());
        dto.setIsChargeable(entity.getIsChargeable());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setImageUrl(entity.getImageUrl());
        return dto;
    }

    // ===================== GET ALL (CÁCH 1) =====================
    public Page<AmenityResponseDTO> getAllAmenities(
            String q,
            String type,
            Pageable pageable
    ) {

        boolean hasQ = q != null && !q.trim().isEmpty();
        boolean hasType = type != null && !type.trim().isEmpty()
                && !type.equalsIgnoreCase("Tất cả loại");

        Page<Amenity> page;

        // 1️⃣ Không search – không filter → LOAD TẤT CẢ
        if (!hasQ && !hasType) {
            page = amenityRepository.findAll(pageable);
        }
        // 2️⃣ Chỉ search
        else if (hasQ && !hasType) {
            page = amenityRepository.searchByNameOrDescription(
                    q.trim(), pageable
            );
        }
        // 3️⃣ Chỉ filter theo loại
        else if (!hasQ) {
            page = amenityRepository.findByCategory(
                    type, pageable
            );
        }
        // 4️⃣ Search + filter
        else {
            page = amenityRepository.searchByNameAndCategory(
                    q.trim(), type, pageable
            );
        }

        return page.map(this::mapToResponseDto);
    }

    // ===================== CREATE =====================
    public AmenityResponseDTO createAmenity(AmenityRequestDTO dto) {
        Amenity saved = amenityRepository.save(mapToEntity(dto));
        return mapToResponseDto(saved);
    }

    // ===================== UPDATE =====================
    public AmenityResponseDTO updateAmenity(Long id, AmenityRequestDTO dto) {
        Amenity existing = amenityRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Không tìm thấy tiện ích với ID: " + id));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setIconClass(dto.getIconClass());
        existing.setCategory(dto.getType());
        existing.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        existing.setIsChargeable(dto.getIsChargeable() != null ? dto.getIsChargeable() : false);
        existing.setImageUrl(dto.getImageUrl());
        return mapToResponseDto(amenityRepository.save(existing));
    }

    // ===================== DELETE =====================
    @Transactional
    public void deleteAmenity(Long id) {

        if (!amenityRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy tiện ích với ID: " + id);
        }

        roomTypeAmenityRepository.deleteByAmenityId(id);
        amenityRepository.deleteById(id);
    }
}
