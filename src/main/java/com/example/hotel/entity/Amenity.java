package com.example.hotel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "amenity")
public class Amenity {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "amenity_id")
    private Long id;

    @Setter
    @Getter
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // Ánh xạ đến cột 'category' trong SQL schema ban đầu
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Setter
    @Getter
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Setter
    @Getter
    @Column(name = "icon_class", length = 100)
    private String iconClass;

    @Setter
    @Getter
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Setter
    @Getter
    @Column(name = "is_chargeable", nullable = false)
    private Boolean isChargeable = false;

    @Setter
    @Getter
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Setter
    @Getter
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Lifer Cycle Callback
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public String getCategory() { return type; }
    public void setCategory(String category) { this.type = category; }

}