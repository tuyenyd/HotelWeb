package com.example.hotel.controller;

import com.example.hotel.dto.AmenityRequestDTO;
import com.example.hotel.dto.AmenityResponseDTO;
import com.example.hotel.service.AmenityService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/amenities")
@PreAuthorize("hasRole('STAFF')")
public class AmenityController {

    @Autowired
    private AmenityService amenityService;

    // --- 1. LẤY DANH SÁCH (READ) ---
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','LEADER','STAFF')")
    public ResponseEntity<Page<AmenityResponseDTO>> getAllAmenities(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 6, sort = "name") Pageable pageable
    ) {
        Page<AmenityResponseDTO> amenities = amenityService.getAllAmenities(q, type, pageable);
        return ResponseEntity.ok(amenities);
    }

    // --- 2. TẠO MỚI (CREATE) ---
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','LEADER','STAFF')")
    public ResponseEntity<AmenityResponseDTO> createAmenity(
            @Valid @RequestBody AmenityRequestDTO amenityDto
    ) {
        AmenityResponseDTO savedDto = amenityService.createAmenity(amenityDto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    // --- 3. CẬP NHẬT (UPDATE) ---
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','LEADER','STAFF')")
    public ResponseEntity<AmenityResponseDTO> updateAmenity(
            @PathVariable Long id,
            @Valid @RequestBody AmenityRequestDTO amenityDto
    ) {
        try {
            AmenityResponseDTO updatedDto = amenityService.updateAmenity(id, amenityDto);
            return ResponseEntity.ok(updatedDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- 4. XÓA (DELETE) ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','LEADER','STAFF')")
    public ResponseEntity<Void> deleteAmenity(@PathVariable Long id) {
        try {
            amenityService.deleteAmenity(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
