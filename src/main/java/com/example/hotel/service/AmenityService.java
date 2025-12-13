package com.example.hotel.service;

import com.example.hotel.dto.AmenityRequestDTO;
import com.example.hotel.dto.AmenityResponseDTO;
import com.example.hotel.entity.Amenity;
import com.example.hotel.repository.AmenityRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AmenityService {

    @Autowired
    private AmenityRepository amenityRepository;


    // --- Mapper: DTO → Entity ---
    private Amenity mapToEntity(AmenityRequestDTO dto) {
        Amenity entity = new Amenity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setIconClass(dto.getIconClass());
        entity.setCategory(dto.getType());

        entity.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        entity.setIsChargeable(dto.getIsChargeable() != null ? dto.getIsChargeable() : false);

        return entity;
    }


    // --- Mapper: Entity → DTO ---
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
        return dto;
    }


    // --- GET ALL (SEARCH / FILTER) ---
    public Page<AmenityResponseDTO> getAllAmenities(String q, String type, Pageable pageable) {

        String query = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        String category = (type != null && !type.isEmpty() && !type.equals("Tất cả loại"))
                ? type
                : null;

        Page<Amenity> amenityPage = amenityRepository.search(query, category, pageable);

        return amenityPage.map(this::mapToResponseDto);
    }


    // --- CREATE ---
    public AmenityResponseDTO createAmenity(AmenityRequestDTO dto) {
        Amenity saved = amenityRepository.save(mapToEntity(dto));
        return mapToResponseDto(saved);
    }


    // --- UPDATE ---
    public AmenityResponseDTO updateAmenity(Long id, AmenityRequestDTO dto) {
        Amenity existing = amenityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tiện ích với ID: " + id));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setIconClass(dto.getIconClass());
        existing.setCategory(dto.getType());
        existing.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        existing.setIsChargeable(dto.getIsChargeable() != null ? dto.getIsChargeable() : false);

        Amenity updated = amenityRepository.save(existing);
        return mapToResponseDto(updated);
    }


    // --- DELETE ---
    @Transactional
    public void deleteAmenity(Long id) {

        if (!amenityRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy tiện ích với ID: " + id);
        }

        // Xóa ràng buộc trước


        // Xóa chính nó
        amenityRepository.deleteById(id);
    }
}