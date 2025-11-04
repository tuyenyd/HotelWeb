package com.example.hotel.repository;

import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(String roomNumber); // Thêm dòng này
    // Override findAll to fetch RoomType eagerly
    @Query("SELECT r FROM Room r JOIN FETCH r.roomType")
    @Override
    List<Room> findAll();

    // Override findById to fetch RoomType eagerly
    @Query("SELECT r FROM Room r JOIN FETCH r.roomType WHERE r.id = :id")
    @Override
    Optional<Room> findById(@Param("id") Long id);

    // Update the searchRooms query to use JOIN FETCH
    @Query("SELECT r FROM Room r JOIN FETCH r.roomType rt " +
            "WHERE (:roomNumber IS NULL OR r.roomNumber LIKE %:roomNumber%) " +
            "AND (:roomTypeId IS NULL OR rt.id = :roomTypeId) " +
            "AND (:status IS NULL OR r.status = :status)")
    List<Room> searchRooms(@Param("roomNumber") String roomNumber,
                           @Param("roomTypeId") Long roomTypeId,
                           @Param("status") RoomStatus status);
}
