package com.example.hotel.controller;

import com.example.hotel.dto.AmenityResponseDTO;
import com.example.hotel.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/amenities")
@PreAuthorize("permitAll()") // 🔓 TOÀN BỘ controller là PUBLIC
public class PublicAmenityController {

    @Autowired
    private AmenityService amenityService;


    @GetMapping
    public Page<AmenityResponseDTO> getPublicAmenities(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 100, sort = "name") Pageable pageable
    ) {
        return amenityService.getAllAmenities(q, type, pageable);
    }
}
