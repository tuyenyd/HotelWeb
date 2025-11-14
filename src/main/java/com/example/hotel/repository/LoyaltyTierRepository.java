package com.example.hotel.repository;

import com.example.hotel.entity.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyTierRepository extends JpaRepository<LoyaltyTier, Long> {

    // Tự động sắp xếp để kiểm tra nâng hạng
    List<LoyaltyTier> findAllByOrderByPointsRequiredDesc();

    // Dùng để tìm hạng Bronze (ID 1)
    LoyaltyTier findFirstByOrderByPointsRequiredAsc();

    Optional<LoyaltyTier> findByName(String name);
}