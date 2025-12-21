package com.example.hotel.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "room_type_amenity")
public class RoomTypeAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_amenity_id")
    private Long id;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Column(name = "amenity_id", nullable = false)
    private Long amenityId;

    @Column(name = "is_included", nullable = false)
    private Boolean isIncluded;

    // Constructors, Getters, Setters (Đã bỏ qua)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }
    public Long getAmenityId() { return amenityId; }
    public void setAmenityId(Long amenityId) { this.amenityId = amenityId; }
    public Boolean getIsIncluded() { return isIncluded; }
    public void setIsIncluded(Boolean isIncluded) { this.isIncluded = isIncluded; }
}