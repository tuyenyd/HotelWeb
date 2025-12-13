package com.example.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "loyalty_tiers")
@Data
public class LoyaltyTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "points_required", nullable = false)
    private Integer pointsRequired;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Lưu các quyền lợi dưới dạng String JSON
    @Column(name = "benefits_json", columnDefinition = "json")
    private String benefitsJson;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;
}