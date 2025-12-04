package com.example.hotel.service;

import com.example.hotel.dto.PriceRuleDto;
import com.example.hotel.entity.RoomType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PriceService {
    // Các hàm quản lý Price Rule (CRUD)
    List<PriceRuleDto> getAllPriceRules();
    PriceRuleDto getPriceRuleById(Long id);
    PriceRuleDto createPriceRule(PriceRuleDto priceRuleDto);
    PriceRuleDto updatePriceRule(Long id, PriceRuleDto priceRuleDto);
    void deletePriceRule(Long id);

    // Các hàm tính toán giá (Quan trọng)
    BigDecimal calculateDailyPrice(RoomType roomType, LocalDate date);
    BigDecimal calculateTotalPrice(RoomType roomType, LocalDate checkIn, LocalDate checkOut);
    BigDecimal calculateTotalBookingPriceById(Long roomId, LocalDate checkIn, LocalDate checkOut);
}