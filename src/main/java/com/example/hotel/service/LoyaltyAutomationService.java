package com.example.hotel.service;

import com.example.hotel.dto.LoyaltyPointTransactionDto;
import com.example.hotel.entity.*;
import com.example.hotel.repository.CustomerRepository;
import com.example.hotel.repository.LoyaltyPointTransactionRepository;
import com.example.hotel.repository.LoyaltyTierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // Thêm thư viện Log để ghi log lỗi
public class LoyaltyAutomationService {

    private final CustomerRepository customerRepository;
    private final LoyaltyTierRepository tierRepository;
    private final LoyaltyPointTransactionRepository transactionRepository;

    /**
     * Hàm chính: Xử lý cộng điểm và nâng hạng khi check-out
     * ĐƯỢC GỌI BỞI BookingServiceImpl
     */
    @Transactional
    public void processBookingCheckout(Booking booking) {

        RoomType roomType = booking.getRoom().getRoomType();
        int pointsEarned = roomType.getPointsEarned();

        // 2. Nếu loại phòng này không có điểm, bỏ qua
        if (pointsEarned <= 0) {
            log.info("Booking {} không có điểm để cộng (loại phòng: {}).",
                    booking.getBookingConfirmationCode(), roomType.getName());
            return;
        }

        Customer customer = booking.getCustomer();

        // 3. Ghi lại giao dịch điểm (lịch sử)
        LoyaltyPointTransaction transaction = new LoyaltyPointTransaction();
        transaction.setCustomer(customer);
        transaction.setBooking(booking);
        transaction.setPointsEarned(pointsEarned);
        transaction.setDescription("Tích điểm cho phòng " + roomType.getName());
        transactionRepository.save(transaction);

        // 4. Cộng điểm cho khách hàng
        customer.setCurrentPoints(customer.getCurrentPoints() + pointsEarned);

        // 5. Kiểm tra và nâng hạng
        updateCustomerTier(customer); // Gọi hàm helper bên dưới

        // 6. Lưu khách hàng (đã có điểm mới và hạng mới)
        customerRepository.save(customer);
        log.info("Đã cộng {} điểm cho khách hàng {}. Tổng điểm: {}. Hạng mới: {}",
                pointsEarned, customer.getId(), customer.getCurrentPoints(), customer.getLoyaltyTier().getName());
    }

    /**
     * Hàm helper để kiểm tra và cập nhật hạng mới cho khách
     */
    private void updateCustomerTier(Customer customer) {
        // Lấy danh sách hạng, sắp xếp điểm từ CAO xuống THẤP
        // (Platinum 3000, Gold 1500, Silver 1000, Bronze 0)
        List<LoyaltyTier> tiers = tierRepository.findAllByOrderByPointsRequiredDesc();

        for (LoyaltyTier tier : tiers) {
            if (customer.getCurrentPoints() >= tier.getPointsRequired()) {
                // Tìm thấy hạng cao nhất mà khách đạt được
                if (!tier.equals(customer.getLoyaltyTier())) {
                    log.info("Khách hàng {} được nâng hạng: {} -> {}",
                            customer.getId(), customer.getLoyaltyTier().getName(), tier.getName());
                    customer.setLoyaltyTier(tier);
                }
                break; // Dừng vòng lặp
            }
        }
    }
    /**
     * Lấy lịch sử giao dịch điểm của một khách hàng.
     * @param customerId ID của khách hàng
     * @return Danh sách DTO lịch sử điểm
     */
    @Transactional(readOnly = true)
    public List<LoyaltyPointTransactionDto> getPointsHistoryByCustomerId(Long customerId) {
        // (Không cần kiểm tra customer tồn tại vì nếu không có giao dịch thì trả về list rỗng)
        List<LoyaltyPointTransaction> transactions = transactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);

        // Chuyển đổi List<LoyaltyPointTransaction> sang List<LoyaltyPointTransactionDto>
        return transactions.stream()
                .map(tx -> new LoyaltyPointTransactionDto(
                        tx.getCreatedAt(),
                        tx.getPointsEarned(),
                        tx.getDescription()
                ))
                .collect(Collectors.toList());
    }
}