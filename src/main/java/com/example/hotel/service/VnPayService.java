package com.example.hotel.service;

import com.example.hotel.entity.Booking;
import com.example.hotel.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class VnPayService {

    private final BookingRepository bookingRepository;

    /**
     * Hàm này sẽ tạo URL thanh toán.
     * Trong thực tế, bạn sẽ dùng SDK của VNPay, mã hóa, và gọi API của họ.
     * Ở đây, chúng ta chỉ giả lập một link trả về.
     */
    public String createPaymentUrl(Long bookingId, BigDecimal amount) {

        // 1. (Thực tế) Lấy thông tin booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy booking: " + bookingId));

        // 2. (Thực tế) Tạo mã giao dịch (vnp_TxnRef) duy nhất
        String txnRef = "ORDER_" + System.currentTimeMillis();

        // 3. (Thực tế) Gọi SDK của VNPay với các tham số:
        // vnp_Amount = amount * 100 (phải nhân 100)
        // vnp_TxnRef = txnRef
        // vnp_OrderInfo = "Thanh toan cho ma dat phong " + booking.getBookingConfirmationCode()
        // vnp_ReturnUrl = "http://localhost:8080/Hotel/HotelUser/payment-result.html"
        // ... (và các tham số khác)

        // 4. (Giả lập) Chúng ta trả thẳng về link "thành công"
        // (Thay thế 'http://localhost:8080/Hotel/HotelUser/...' bằng đường dẫn của bạn)
        String successUrl = "http://localhost:8080/Hotel/HotelUser/payment-result.html" +
                "?vnp_Amount=" + amount +
                "&vnp_BankCode=NCB" +
                "&vnp_OrderInfo=Thanh+toan+gia+lap+thanh+cong" +
                "&vnp_ResponseCode=00" + // 00 = Thành công
                "&vnp_TxnRef=" + txnRef +
                "&bookingId=" + bookingId;

        // Lưu txnRef này vào booking của bạn để đối soát
        // booking.setPaymentTxnRef(txnRef);
        // bookingRepository.save(booking);

        return successUrl;
    }
}