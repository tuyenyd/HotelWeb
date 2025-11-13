package com.example.hotel.service.impl;

import com.example.hotel.dto.RoomDto;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomStatus;
import com.example.hotel.entity.RoomType;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.repository.RoomTypeRepository;
import com.example.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoomDto> getRoomById(Long id) {
        return roomRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional
    public RoomDto createRoom(RoomDto roomDto) {
        // Kiểm tra số phòng trùng lặp khi tạo mới ===
        if (roomRepository.findByRoomNumber(roomDto.getRoomNumber()).isPresent()) {
            throw new DataIntegrityViolationException("Room number '" + roomDto.getRoomNumber() + "' already exists.");
        }

        Room room = convertToEntity(roomDto);
        Room savedRoom = roomRepository.save(room);
        return convertToDto(savedRoom);
    }

    @Override
    @Transactional
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        // Kiểm tra số phòng trùng lặp khi cập nhật ===
        Optional<Room> roomWithSameNumber = roomRepository.findByRoomNumber(roomDto.getRoomNumber());
        if (roomWithSameNumber.isPresent() && !roomWithSameNumber.get().getId().equals(id)) {
            // Tìm thấy một phòng có cùng số, nhưng đó không phải là phòng đang được chỉnh sửa
            throw new DataIntegrityViolationException("Room number '" + roomDto.getRoomNumber() + "' is already used by another room.");
        }
        // =============================================================

        existingRoom.setRoomNumber(roomDto.getRoomNumber());
        existingRoom.setStatus(RoomStatus.valueOf(roomDto.getStatus()));
        existingRoom.setFloor(roomDto.getFloor());

        // Cập nhật giá nếu được cung cấp
        if (roomDto.getPricePerNight() != null) {
            existingRoom.setPrice(roomDto.getPricePerNight());
        }

        if (roomDto.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findById(roomDto.getRoomTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("RoomType not found with id: " + roomDto.getRoomTypeId()));
            existingRoom.setRoomType(roomType);
        }

        Room updatedRoom = roomRepository.save(existingRoom);
        return convertToDto(updatedRoom);
    }


    @Override
    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> searchRooms(String roomNumber, Long roomTypeId, String status) {
        RoomStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = RoomStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {

        } // Handle invalid status string
    }
        return roomRepository.searchRooms(roomNumber, roomTypeId, statusEnum)
                .stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private RoomDto convertToDto(Room room) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setStatus(room.getStatus().name());
        dto.setFloor(room.getFloor());

        if (room.getRoomType() != null) {
            RoomType roomType = room.getRoomType();
            dto.setRoomTypeId(roomType.getId());
            dto.setRoomTypeName(roomType.getName());
            dto.setPricePerNight(roomType.getBasePrice());
            dto.setCapacity(roomType.getCapacity());
            dto.setDescription(roomType.getDescription());

            String amenitiesStr = roomType.getAmenities();
            if (amenitiesStr != null && !amenitiesStr.trim().isEmpty()) {
                dto.setAmenities(Arrays.asList(amenitiesStr.trim().split("\\s*,\\s*")));
            } else {
                dto.setAmenities(Collections.emptyList());
            }
        }
        return dto;
    }

    private Room convertToEntity(RoomDto roomDto) {
        Room room = new Room();

        if (roomDto.getRoomTypeId() == null) {
            throw new IllegalArgumentException("Room Type ID must not be null.");
        }

        RoomType roomType = roomTypeRepository.findById(roomDto.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("RoomType not found with id: " + roomDto.getRoomTypeId()));

        room.setRoomNumber(roomDto.getRoomNumber());
        room.setRoomType(roomType);
        room.setStatus(RoomStatus.valueOf(roomDto.getStatus()));
        room.setFloor(roomDto.getFloor());

        if (roomDto.getPricePerNight() != null) {
            room.setPrice(roomDto.getPricePerNight());
        } else {
            room.setPrice(roomType.getBasePrice());
        }

        return room;
    }
    @Override
    @Transactional
    public RoomDto updateRoomStatus(Long id, String status) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        try {
            RoomStatus newStatus = RoomStatus.valueOf(status.toUpperCase());
            existingRoom.setStatus(newStatus);
            Room updatedRoom = roomRepository.save(existingRoom);
            return convertToDto(updatedRoom);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid room status: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> findAvailableRooms(LocalDate checkin, LocalDate checkout, int totalGuests) {
        if (checkin.isAfter(checkout) || checkin.isEqual(checkout)) {
            throw new IllegalArgumentException("Ngày check-out phải sau ngày check-in.");
        }

        // 1. Gọi Repository
        List<Room> rooms = roomRepository.findAvailableRooms(checkin, checkout, totalGuests);

        // 2. Chuyển List<Room> thành List<RoomDto>
        return rooms.stream().map(room -> {
            RoomDto dto = new RoomDto();
            dto.setId(room.getId());
            dto.setRoomNumber(room.getRoomNumber());
            dto.setStatus(room.getStatus().name());
            dto.setFloor(room.getFloor());

            // Lấy thông tin từ RoomType
            if (room.getRoomType() != null) {
                RoomType rt = room.getRoomType();
                dto.setRoomTypeId(rt.getId());
                dto.setRoomTypeName(rt.getName());
                dto.setCapacity(rt.getCapacity());
                dto.setDescription(rt.getDescription());

                // Lấy tiện nghi (amenities)
                String amenitiesStr = rt.getAmenities();
                if (amenitiesStr != null && !amenitiesStr.trim().isEmpty()) {
                    dto.setAmenities(Arrays.asList(amenitiesStr.trim().split("\\s*,\\s*")));
                } else {
                    dto.setAmenities(Collections.emptyList());
                }
            }

            // Quan trọng: Dùng giá của Room (room.getPrice())
            // Hàm convertToDto cũ của bạn đang dùng roomType.getBasePrice() là không chính xác
            dto.setPricePerNight(room.getPrice());

            return dto;
        }).collect(Collectors.toList());
    }
}
