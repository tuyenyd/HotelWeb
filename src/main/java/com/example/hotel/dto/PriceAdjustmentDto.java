package com.example.hotel.dto;

import com.example.hotel.entity.PriceAdjustment;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceAdjustmentDto {
    private Long id;
    private Long roomTypeId;
    private String roomTypeName; // Để hiển thị cho tiện
    private BigDecimal basePrice; // Giá gốc để tham khảo
    private PriceAdjustment.AdjustmentType adjustmentType;
    private BigDecimal adjustmentValue;
}