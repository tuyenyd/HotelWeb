package com.example.hotel.repository;

import com.example.hotel.entity.PriceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {

    // Tìm tất cả các quy tắc đang kích hoạt mà khoảng thời gian của nó bao trùm ngày được truyền vào.
    // Sắp xếp kết quả theo độ ưu tiên giảm dần (ưu tiên cao nhất lên đầu).
    // Sử dụng JOIN FETCH để tải luôn danh sách adjustments để tránh lỗi N+1 query.
    @Query("SELECT pr FROM PriceRule pr LEFT JOIN FETCH pr.adjustments WHERE pr.isActive = true AND :targetDate >= pr.startDate AND :targetDate <= pr.endDate ORDER BY pr.priority DESC")
    List<PriceRule> findActiveRulesForDate(@Param("targetDate") LocalDate targetDate);
}