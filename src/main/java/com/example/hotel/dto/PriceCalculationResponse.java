package com.example.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceCalculationResponse {
    private BigDecimal originalPrice;   // Giá gốc (VD: 2.000.000)
    private BigDecimal discountAmount;  // Số tiền được giảm (VD: 200.000)
    private BigDecimal finalPrice;      // Tổng cộng phải trả (VD: 1.800.000)
    private String appliedDiscountName; // Tên ưu đãi (VD: "Hạng Vàng -10%")
    // Có thể thêm điểm dự kiến tích được nếu muốn: private Long estimatedPoints;
}