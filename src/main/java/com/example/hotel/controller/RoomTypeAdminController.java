package com.example.hotel.controller;

import com.example.hotel.dto.RoomTypeDto;
import com.example.hotel.service.RoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/api/admin/room-types")
public class RoomTypeAdminController {

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping
    public ResponseEntity<List<RoomTypeDto>> getAllRoomTypes() {
        List<RoomTypeDto> roomTypes = roomTypeService.getAllRoomTypes();
        return ResponseEntity.ok(roomTypes);
    }


    @GetMapping("/{id}")
    public ResponseEntity<RoomTypeDto> getRoomTypeById(@PathVariable Long id) {
        Optional<RoomTypeDto> roomTypeDto = roomTypeService.getRoomTypeById(id);
        // Sửa lỗi ambiguous bằng cách dùng lambda expression
        return roomTypeDto.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    public ResponseEntity<RoomTypeDto> createRoomType(@RequestBody RoomTypeDto roomTypeDto) {
        RoomTypeDto createdRoomType = roomTypeService.createRoomType(roomTypeDto);
        return new ResponseEntity<>(createdRoomType, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomTypeDto> updateRoomType(@PathVariable Long id, @RequestBody RoomTypeDto roomTypeDto) {
        RoomTypeDto updatedRoomType = roomTypeService.updateRoomType(id, roomTypeDto);
        return ResponseEntity.ok(updatedRoomType);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ResponseEntity.noContent().build();
    }
}
