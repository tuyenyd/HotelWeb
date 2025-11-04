package com.example.hotel.service.impl;

import com.example.hotel.dto.RoomTypeDto;
import com.example.hotel.entity.RoomType;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.repository.RoomTypeRepository;
import com.example.hotel.service.RoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomTypeServiceImpl implements RoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    // --- START: HÀM TIỆN ÍCH ĐỂ TẠO TYPE_CODE ---
    private String generateTypeCode(String name) {
        if (name == null || name.isEmpty()) {
            return "DEFAULT_CODE"; // Hoặc ném ra một exception
        }
        // Chuyển chuỗi có dấu thành không dấu
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // Thay thế 'Đ' và 'đ'
        normalized = normalized.replaceAll("[Đđ]", "D");
        // Chuyển thành chữ hoa và thay thế khoảng trắng/ký tự đặc biệt bằng dấu gạch dưới
        return normalized.toUpperCase().replaceAll("\\s+", "_").replaceAll("[^A-Z0-9_]", "");
    }
    // --- END: HÀM TIỆN ÍCH ---

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeDto> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    // Thêm phương thức này vào lớp RoomTypeServiceImpl
    @Override
    public Optional<RoomTypeDto> getRoomTypeById(Long id) {
        return roomTypeRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional
    public RoomTypeDto createRoomType(RoomTypeDto roomTypeDto) {
        RoomType roomType = convertToEntity(roomTypeDto);
        roomType.setTypeCode(generateTypeCode(roomType.getName()));
        RoomType savedRoomType = roomTypeRepository.save(roomType);
        return convertToDto(savedRoomType);
    }

    @Override
    @Transactional
    public RoomTypeDto updateRoomType(Long id, RoomTypeDto roomTypeDto) {
        RoomType existingRoomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType not found with id: " + id));

        existingRoomType.setName(roomTypeDto.getName());
        existingRoomType.setDescription(roomTypeDto.getDescription());
        existingRoomType.setCapacity(roomTypeDto.getCapacity());
        existingRoomType.setBasePrice(roomTypeDto.getBasePrice());
        existingRoomType.setArea(roomTypeDto.getArea());
        existingRoomType.setTypeCode(generateTypeCode(roomTypeDto.getName()));
        existingRoomType.setPointsEarned(roomTypeDto.getPointsEarned());
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
    public void deleteRoomType(Long id) {
        if (!roomTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("RoomType not found with id: " + id);
        }
        roomTypeRepository.deleteById(id);
    }

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
        if (roomType.getAmenities() != null && !roomType.getAmenities().isEmpty()) {
            dto.setAmenities(Arrays.asList(roomType.getAmenities().split(",")));
        } else {
            dto.setAmenities(Collections.emptyList());
        }
        dto.setRoomCount(roomType.getRooms() != null ? roomType.getRooms().size() : 0);
        return dto;
    }

    private RoomType convertToEntity(RoomTypeDto dto) {
        RoomType roomType = new RoomType();
        roomType.setId(dto.getId());
        roomType.setName(dto.getName());
        roomType.setDescription(dto.getDescription());
        roomType.setCapacity(dto.getCapacity());
        roomType.setBasePrice(dto.getBasePrice());
        roomType.setArea(dto.getArea());
        if (dto.getAmenities() != null) {
            roomType.setAmenities(String.join(",", dto.getAmenities()));
        }
        return roomType;
    }
}
