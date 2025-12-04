package com.example.hotel.service.impl;

import com.example.hotel.dto.PriceAdjustmentDto;
import com.example.hotel.dto.PriceRuleDto;
import com.example.hotel.entity.PriceAdjustment;
import com.example.hotel.entity.PriceRule;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomType;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.repository.PriceRuleRepository;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.repository.RoomTypeRepository;
import com.example.hotel.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final PriceRuleRepository priceRuleRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;

    @Override
    public BigDecimal calculateDailyPrice(RoomType roomType, LocalDate date) {
        BigDecimal finalPrice = roomType.getBasePrice();
        List<PriceRule> applicableRules = priceRuleRepository.findActiveRulesForDate(date);

        if (applicableRules.isEmpty()) {
            return finalPrice;
        }

        PriceRule highestPriorityRule = applicableRules.getFirst(); // Sử dụng getFirst() thay cho get(0)

        if (highestPriorityRule.getAdjustments() == null) {
            return finalPrice;
        }

        PriceAdjustment adjustment = highestPriorityRule.getAdjustments().stream()
                .filter(adj -> adj.getRoomType().getId().equals(roomType.getId()))
                .findFirst()
                .orElse(null);

        if (adjustment != null) {
            BigDecimal adjValue = adjustment.getAdjustmentValue();
            if (adjustment.getAdjustmentType() == PriceAdjustment.AdjustmentType.PERCENTAGE) {
                BigDecimal changeAmount = finalPrice.multiply(adjValue)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                finalPrice = finalPrice.add(changeAmount);
            } else if (adjustment.getAdjustmentType() == PriceAdjustment.AdjustmentType.FIXED_AMOUNT) {
                finalPrice = finalPrice.add(adjValue);
            }
        }
        return finalPrice.max(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal calculateTotalPrice(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new IllegalArgumentException("Ngày check-in phải trước ngày check-out");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        long numOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);

        for (int i = 0; i < numOfNights; i++) {
            LocalDate currentDate = checkIn.plusDays(i);
            BigDecimal dailyPrice = calculateDailyPrice(roomType, currentDate);
            totalPrice = totalPrice.add(dailyPrice);
        }
        return totalPrice;
    }

    @Override
    public List<PriceRuleDto> getAllPriceRules() {
        return priceRuleRepository.findAll().stream()
                .map(this::convertToDto)
                .toList(); // Sử dụng toList() cho gọn
    }

    @Override
    public PriceRuleDto getPriceRuleById(Long id) {
        PriceRule priceRule = priceRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Price rule not found with id: " + id));
        return convertToDto(priceRule);
    }

    @Override
    @Transactional
    @CacheEvict(value = "rooms", key = "'all'")
    public PriceRuleDto createPriceRule(PriceRuleDto priceRuleDto) {
        PriceRule priceRule = convertToEntity(priceRuleDto);
        PriceRule savedRule = priceRuleRepository.save(priceRule);
        return convertToDto(savedRule);
    }

    @Override
    @Transactional
    @CacheEvict(value = "rooms", key = "'all'")
    public PriceRuleDto updatePriceRule(Long id, PriceRuleDto priceRuleDto) {
        PriceRule existingRule = priceRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Price rule not found with id: " + id));

        existingRule.setName(priceRuleDto.getName());
        existingRule.setStartDate(priceRuleDto.getStartDate());
        existingRule.setEndDate(priceRuleDto.getEndDate());
        existingRule.setPriority(priceRuleDto.getPriority());
        existingRule.setIsActive(priceRuleDto.getIsActive());


// 1. Lấy danh sách hiện tại (Hibernate đang theo dõi cái này)
        List<PriceAdjustment> currentAdjustments = existingRule.getAdjustments();

// 2. Xóa sạch nội dung cũ.
// Nhờ orphanRemoval=true, Hibernate sẽ đánh dấu các phần tử bị xóa ở đây để DELETE khỏi DB.
        currentAdjustments.clear();

// 3. Thêm các phần tử mới vào danh sách hiện tại
        if (priceRuleDto.getAdjustments() != null) {
            for (PriceAdjustmentDto adjDto : priceRuleDto.getAdjustments()) {
                PriceAdjustment adjustment = convertAdjustmentToEntity(adjDto);
                // Quan trọng: Thiết lập quan hệ 2 chiều
                adjustment.setPriceRule(existingRule);
                // Thêm vào danh sách hiện tại
                currentAdjustments.add(adjustment);
            }
        }


        PriceRule updatedRule = priceRuleRepository.save(existingRule);
        return convertToDto(updatedRule);
    }

    @Override
    @Transactional
    @CacheEvict(value = "rooms", key = "'all'")
    public void deletePriceRule(Long id) {
        if (!priceRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Price rule not found with id: " + id);
        }
        priceRuleRepository.deleteById(id);
    }
    @Override
    public BigDecimal calculateTotalBookingPriceById(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        // 1. Kiểm tra ngày hợp lệ
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new IllegalArgumentException("Ngày check-in phải trước ngày check-out");
        }

        // 2. Tìm thông tin phòng để lấy RoomType và giá gốc
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        // Giả sử giá gốc lấy từ RoomType.
        // Nếu hệ thống của bạn cho phép set giá riêng cho từng Room, bạn cần điều chỉnh logic này.
        RoomType roomType = room.getRoomType();
        if (roomType == null) {
            throw new IllegalStateException("Room does not have a room type assigned.");
        }


        // 3. Tính toán tổng tiền (Lặp qua từng đêm)
        BigDecimal totalPrice = BigDecimal.ZERO;
        long numOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);

        for (int i = 0; i < numOfNights; i++) {
            LocalDate currentDate = checkIn.plusDays(i);
            // Tận dụng hàm tính giá theo ngày đã có
            BigDecimal dailyPrice = calculateDailyPrice(roomType, currentDate);
            totalPrice = totalPrice.add(dailyPrice);
        }

        return totalPrice;
    }

    private PriceRuleDto convertToDto(PriceRule entity) {
        PriceRuleDto dto = new PriceRuleDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setPriority(entity.getPriority());
        dto.setIsActive(entity.getIsActive());

        if (entity.getAdjustments() != null) {
            List<PriceAdjustmentDto> adjDtos = entity.getAdjustments().stream()
                    .map(this::convertAdjustmentToDto)
                    .toList(); // Sử dụng toList()
            dto.setAdjustments(adjDtos);
        }
        return dto;
    }

    // SỬA LỖI: Đổi kiểu trả về từ PriceAdjustment sang PriceAdjustmentDto
    private PriceAdjustmentDto convertAdjustmentToDto(PriceAdjustment entity) {
        PriceAdjustmentDto dto = new PriceAdjustmentDto();
        dto.setId(entity.getId());
        dto.setAdjustmentType(entity.getAdjustmentType());
        dto.setAdjustmentValue(entity.getAdjustmentValue());

        if (entity.getRoomType() != null) {
            dto.setRoomTypeId(entity.getRoomType().getId());
            dto.setRoomTypeName(entity.getRoomType().getName());
            dto.setBasePrice(entity.getRoomType().getBasePrice());
        }
        return dto;
    }

    private PriceRule convertToEntity(PriceRuleDto dto) {
        PriceRule entity = new PriceRule();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setPriority(dto.getPriority());
        entity.setIsActive(dto.getIsActive());
        entity.setAdjustments(new ArrayList<>());

        if (dto.getAdjustments() != null) {
            for (PriceAdjustmentDto adjDto : dto.getAdjustments()) {
                PriceAdjustment adjustmentEntity = convertAdjustmentToEntity(adjDto);
                entity.addAdjustment(adjustmentEntity);
            }
        }
        return entity;
    }

    private PriceAdjustment convertAdjustmentToEntity(PriceAdjustmentDto dto) {
        PriceAdjustment entity = new PriceAdjustment();
        entity.setId(dto.getId());
        entity.setAdjustmentType(dto.getAdjustmentType());
        entity.setAdjustmentValue(dto.getAdjustmentValue());

        RoomType roomType = roomTypeRepository.findById(dto.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with id: " + dto.getRoomTypeId()));
        entity.setRoomType(roomType);
        return entity;
    }
}
