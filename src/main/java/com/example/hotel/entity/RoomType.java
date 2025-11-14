package com.example.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "room_types")
@Data
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_code", nullable = false, unique = true)
    private String typeCode;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column
    private Double area; // Diện tích (m²)

    @Column(name = "amenities")
    private String amenities;

    @OneToMany(mappedBy = "roomType", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Room> rooms;

    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned = 0; // Điểm cố định cho loại phòng này
}
