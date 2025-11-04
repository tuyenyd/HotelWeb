package com.example.hotel.repository;

import com.example.hotel.entity.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyTierRepository extends JpaRepository<LoyaltyTier, Long> {

    // Tự động sắp xếp để kiểm tra nâng hạng
    List<LoyaltyTier> findAllByOrderByPointsRequiredDesc();

    // Dùng để tìm hạng Bronze (ID 1)
    // Hoặc bạn có thể dùng findById(1L)
    LoyaltyTier findFirstByOrderByPointsRequiredAsc();
}