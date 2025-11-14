package com.example.hotel.repository;

import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(String roomNumber);
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

    @Query("SELECT r FROM Room r JOIN r.roomType rt " +
            "WHERE rt.capacity >= :totalGuests " +
            "AND r.status = 'AVAILABLE' " +
            "AND r.id NOT IN (" +
            "  SELECT b.room.id FROM Booking b " +
            "  WHERE b.status != 'CANCELLED' " +
            "  AND (b.checkInDate < :checkout AND b.checkOutDate > :checkin)" +
            ")")
    List<Room> findAvailableRooms(
            @Param("checkin") LocalDate checkin,
            @Param("checkout") LocalDate checkout,
            @Param("totalGuests") int totalGuests
    );
}
