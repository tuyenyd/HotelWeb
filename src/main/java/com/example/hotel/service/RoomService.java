package com.example.hotel.service;

import com.example.hotel.dto.RoomDto;
import com.example.hotel.dto.RoomTypeDto;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    List<RoomDto> getAllRooms();
    Optional<RoomDto> getRoomById(Long id);
    RoomDto createRoom(RoomDto roomDto);
    RoomDto updateRoom(Long id, RoomDto roomDto);
    void deleteRoom(Long id);
    List<RoomDto> searchRooms(String roomNumber, Long roomTypeId, String status);
    RoomDto updateRoomStatus(Long id, String status);
}
