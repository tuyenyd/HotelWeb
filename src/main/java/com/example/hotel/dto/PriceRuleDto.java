package com.example.hotel.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PriceRuleDto {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer priority;
    private Boolean isActive;
    private List<PriceAdjustmentDto> adjustments;
}