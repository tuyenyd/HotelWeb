package com.example.hotel.service;

import com.example.hotel.dto.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    Page<BookingDto> findBookings(String status, LocalDate fromDate, LocalDate toDate, Long roomTypeId, String search, Pageable pageable);
    BookingDto getBookingById(Long id);
    BookingDto createBooking(BookingDto bookingDto);
    BookingDto updateBooking(Long id, BookingDto bookingDto);
    void deleteBooking(Long id);
    BookingDto updateBookingStatus(Long id, String status);
    /**
     * Lấy lịch sử đặt phòng của một khách hàng.
     * @param customerId ID của khách hàng
     * @return Danh sách DTO lịch sử đặt phòng
     */
    List<BookingHistoryDto> getBookingHistoryByCustomerId(Long customerId);
    /**
    * Lấy danh sách các booking đã bị xóa (trong thùng rác) - có phân trang.
     * @param pageable Thông tin phân trang
     * @return Một trang (Page) các BookingDto
     */
    Page<BookingDto> getDeletedBookings(Pageable pageable);

    /**
     * Tạo đặt phòng công khai (từ trang người dùng)
     * @param request DTO chứa thông tin từ form
     * @param token (Nullable) JWT token của khách hàng nếu họ đã đăng nhập
     * @return DTO chứa mã đặt phòng
     */
    BookingResponseDTO createPublicBooking(BookingRequestDTO request, String token);
    /**
     * Lấy chi tiết đặt phòng cho khách hàng (có kiểm tra quyền sở hữu)
     * @param customerEmail Email của khách hàng (từ token)
     * @param bookingId ID của đặt phòng
     * @return DTO chi tiết
     * @throws EntityNotFoundException nếu không tìm thấy booking
     * @throws AccessDeniedException nếu booking không thuộc về khách hàng
     */
    CustomerBookingDetailDTO getBookingDetailsForCustomer(String customerEmail, Long bookingId)
            throws EntityNotFoundException, AccessDeniedException;

    void processPaymentSuccess(Long bookingId, BigDecimal amount, String txnRef);

    void cancelBookingByCustomer(String email, Long bookingId);

    void processRealtimePayment(String bookingCode, BigDecimal amount, String txnRef);
}

