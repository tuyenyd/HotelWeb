package com.example.hotel.repository;

import com.example.hotel.entity.RoomTypeAmenity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomTypeAmenityRepository extends JpaRepository<RoomTypeAmenity, Long> {

    // Xóa tất cả các liên kết của tiện ích này
    @Modifying
    @Transactional
    void deleteByAmenityId(Long amenityId);
}