package com.example.hotel.repository;

import com.example.hotel.entity.LoyaltyPointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyPointTransactionRepository extends JpaRepository<LoyaltyPointTransaction, Long> {

    /**
     * Tìm tất cả giao dịch điểm của một khách hàng, sắp xếp theo ngày tạo mới nhất trước.
     * @param customerId ID của khách hàng
     * @return Danh sách các giao dịch điểm
     */
    List<LoyaltyPointTransaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

}