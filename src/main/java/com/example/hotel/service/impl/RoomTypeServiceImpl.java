package com.example.hotel.service.impl;

import com.example.hotel.dto.RoomTypeDto;
import com.example.hotel.entity.RoomType;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.repository.RoomTypeRepository;
import com.example.hotel.service.PriceService;
import com.example.hotel.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final PriceService priceService;

    private String generateTypeCode(String name) {
        if (name == null || name.isEmpty()) {
            return "DEFAULT_CODE";
        }
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replaceAll("[Đđ]", "D");
        return normalized.toUpperCase().replaceAll("\\s+", "_").replaceAll("[^A-Z0-9_]", "");
    }

    @Override
    @Transactional(readOnly = true)
    // ĐÃ BỎ @Cacheable ở đây vì giá thay đổi hàng ngày
    public List<RoomTypeDto> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::convertToDto) // Chỉ gọi convertToDto, việc tính giá nằm trong đó
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    // ĐÃ BỎ @Cacheable ở đây vì giá thay đổi hàng ngày
    public Optional<RoomTypeDto> getRoomTypeById(Long id) {
        return roomTypeRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roomTypes", key = "'all'")
    public RoomTypeDto createRoomType(RoomTypeDto roomTypeDto) {
        RoomType roomType = convertToEntity(roomTypeDto);
        roomType.setTypeCode(generateTypeCode(roomType.getName()));
        RoomType savedRoomType = roomTypeRepository.save(roomType);
        return convertToDto(savedRoomType);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "roomTypes", key = "'all'"),
            // Vẫn giữ evict theo ID để đảm bảo nếu có cơ chế cache khác map theo ID thì nó cũng được làm mới
            @CacheEvict(value = "roomType", key = "#id")
    })
    public RoomTypeDto updateRoomType(Long id, RoomTypeDto roomTypeDto) {
        RoomType existingRoomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType not found with id: " + id));

        existingRoomType.setName(roomTypeDto.getName());
        existingRoomType.setDescription(roomTypeDto.getDescription());
        existingRoomType.setCapacity(roomTypeDto.getCapacity());
        existingRoomType.setBasePrice(roomTypeDto.getBasePrice());
        existingRoomType.setArea(roomTypeDto.getArea());
        // Chỉ cập nhật typeCode nếu tên thay đổi (tùy chọn nghiệp vụ)
        if (!existingRoomType.getName().equals(roomTypeDto.getName())) {
            existingRoomType.setTypeCode(generateTypeCode(roomTypeDto.getName()));
        }
        existingRoomType.setPointsEarned(roomTypeDto.getPointsEarned());
        existingRoomType.setImageUrl(roomTypeDto.getImageUrl());
        existingRoomType.setGalleryImages(roomTypeDto.getGalleryImages());
        existingRoomType.setOverview(roomTypeDto.getOverview());

        if (roomTypeDto.getAmenities() != null) {
            existingRoomType.setAmenities(String.join(",", roomTypeDto.getAmenities()));
        } else {
            existingRoomType.setAmenities(null);
        }

        RoomType updatedRoomType = roomTypeRepository.save(existingRoomType);
        return convertToDto(updatedRoomType);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "roomTypes", key = "'all'"),
            @CacheEvict(value = "roomType", key = "#id")
    })
    public void deleteRoomType(Long id) {
        if (!roomTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("RoomType not found with id: " + id);
        }
        // Cần kiểm tra xem có phòng nào đang sử dụng loại phòng này không trước khi xóa (Ràng buộc khóa ngoại)
        // Nếu DB chưa thiết lập ON DELETE CASCADE hoặc set NULL, lệnh này sẽ lỗi nếu còn phòng con.
        roomTypeRepository.deleteById(id);
    }

    // === HÀM MAPPING VÀ TÍNH GIÁ TẬP TRUNG ===
    private RoomTypeDto convertToDto(RoomType roomType) {
        RoomTypeDto dto = new RoomTypeDto();
        dto.setId(roomType.getId());
        dto.setName(roomType.getName());
        dto.setTypeCode(roomType.getTypeCode());
        dto.setDescription(roomType.getDescription());
        dto.setCapacity(roomType.getCapacity());
        dto.setBasePrice(roomType.getBasePrice());
        dto.setArea(roomType.getArea());
        dto.setPointsEarned(roomType.getPointsEarned());
        dto.setOverview(roomType.getOverview());
        dto.setImageUrl(roomType.getImageUrl());
        dto.setGalleryImages(roomType.getGalleryImages());

        // --- TÍNH GIÁ CHO NGÀY HÔM NAY (Duy nhất tại đây) ---
        BigDecimal todayPrice = priceService.calculateDailyPrice(roomType, LocalDate.now());
        dto.setCurrentPrice(todayPrice);
        // ----------------------------------------------------

        if (roomType.getAmenities() != null && !roomType.getAmenities().isEmpty()) {
            dto.setAmenities(Arrays.asList(roomType.getAmenities().split(",")));
        } else {
            dto.setAmenities(Collections.emptyList());
        }
        // Kiểm tra null cho list rooms để tránh NullPointerException
        dto.setRoomCount(roomType.getRooms() != null ? roomType.getRooms().size() : 0);
        return dto;
    }

    private RoomType convertToEntity(RoomTypeDto dto) {
        RoomType roomType = new RoomType();
        // Không set ID khi tạo mới, ID tự tăng

        roomType.setName(dto.getName());
        roomType.setDescription(dto.getDescription());
        roomType.setCapacity(dto.getCapacity());
        roomType.setBasePrice(dto.getBasePrice());
        roomType.setArea(dto.getArea());
        roomType.setPointsEarned(dto.getPointsEarned());
        roomType.setImageUrl(dto.getImageUrl());
        roomType.setGalleryImages(dto.getGalleryImages());
        roomType.setOverview(dto.getOverview());

        if (dto.getAmenities() != null) {
            roomType.setAmenities(String.join(",", dto.getAmenities()));
        }
        return roomType;
    }
}