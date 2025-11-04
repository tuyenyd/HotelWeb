package com.example.hotel.service;

import com.example.hotel.dto.RoomTypeDto;
import java.util.List;
import java.util.Optional;

public interface RoomTypeService {
    List<RoomTypeDto> getAllRoomTypes();

    // Thêm phương thức này vào lớp RoomTypeServiceImpl
    Optional<RoomTypeDto> getRoomTypeById(Long id);

    RoomTypeDto createRoomType(RoomTypeDto roomTypeDto);
    RoomTypeDto updateRoomType(Long id, RoomTypeDto roomTypeDto);
    void deleteRoomType(Long id);
}
