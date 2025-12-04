package com.example.hotel.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "price_adjustments")
@Data
@NoArgsConstructor
public class PriceAdjustment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_rule_id", nullable = false)
    private PriceRule priceRule;

    @ManyToOne(fetch = FetchType.EAGER) // Eager để lấy luôn thông tin loại phòng khi cần
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false, length = 20)
    private AdjustmentType adjustmentType;

    @Column(name = "adjustment_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal adjustmentValue;

    public enum AdjustmentType {
        PERCENTAGE,
        FIXED_AMOUNT
    }
}