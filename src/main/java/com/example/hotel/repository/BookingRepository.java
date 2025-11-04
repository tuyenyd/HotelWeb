package com.example.hotel.repository;

import com.example.hotel.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    /**
     * Tìm tất cả đặt phòng của một khách hàng, sắp xếp theo ngày check-in mới nhất trước.
     * @param customerId ID của khách hàng
     * @return Danh sách các đặt phòng
     */
    List<Booking> findByCustomerIdOrderByCheckInDateDesc(Long customerId);
    /**
     * Tùy chọn: Nếu cần tìm MỘT booking cụ thể trong thùng rác
     */
    @Query(
            value = "SELECT * FROM bookings WHERE deleted = true",
            nativeQuery = true
    )
    Page<Booking> findAllDeleted(Pageable pageable);

    @Query(
            value = "SELECT * FROM bookings WHERE id = :id AND deleted = true",
            nativeQuery = true
    )
    Optional<Booking> findDeletedById(@Param("id") Long id);
}

