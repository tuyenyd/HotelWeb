package com.example.hotel.service.impl;

import com.example.hotel.dto.CustomerRequestDTO;
import com.example.hotel.dto.CustomerResponseDTO;
import com.example.hotel.entity.Booking;
import com.example.hotel.entity.BookingStatus;
import com.example.hotel.entity.Customer;
import com.example.hotel.entity.LoyaltyTier;
import com.example.hotel.repository.BookingRepository;
import com.example.hotel.repository.LoyaltyTierRepository;
import com.example.hotel.repository.CustomerRepository;
import com.example.hotel.service.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final LoyaltyTierRepository loyaltyTierRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CustomerResponseDTO createCustomer(CustomerRequestDTO customerRequest) {
        // Kiểm tra xem email hoặc idNumber đã tồn tại chưa
        if (customerRepository.findByEmail(customerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (customerRepository.findByIdNumber(customerRequest.getIdNumber()).isPresent()) {
            throw new IllegalArgumentException("Số ID (CCCD) đã tồn tại");
        }

        // Chuyển DTO thành Entity
        Customer customer = mapToEntity(customerRequest);

        // 1. Tìm hạng Bronze (ID 1, hoặc hạng có điểm yêu cầu thấp nhất)
        LoyaltyTier defaultTier = loyaltyTierRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hạng mặc định (ID 1). Vui lòng kiểm tra CSDL."));

        // 2. Gán điểm và hạng cho khách hàng mới
        customer.setCurrentPoints(0);
        customer.setLoyaltyTier(defaultTier);

        // Lưu vào DB
        Customer savedCustomer = customerRepository.save(customer);

        // Chuyển Entity đã lưu thành Response DTO (phiên bản mới không cần list booking)
        return mapToResponseDTO(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true) // Thêm Transactional vì có truy vấn DB
    public CustomerResponseDTO getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với ID: " + customerId));
        // Lấy booking chỉ cho khách hàng này để tính toán
        List<Booking> customerBookings = bookingRepository.findByCustomerIdOrderByCheckInDateDesc(customerId);
        return mapToResponseDTO(customer, customerBookings);
    }

    @Override
    @Transactional(readOnly = true) // Thêm Transactional vì có truy vấn DB
    public List<CustomerResponseDTO> getAllCustomers() {
        // Lấy tất cả khách hàng
        List<Customer> customers = customerRepository.findAll();
        // Lấy tất cả booking (để tối ưu, tránh N+1 query)
        // Lưu ý: Nếu có quá nhiều booking, cách này có thể tốn bộ nhớ.
        // Cân nhắc dùng query tổng hợp (GROUP BY) nếu hiệu năng là vấn đề.
        List<Booking> allBookings = bookingRepository.findAll();

        return customers.stream()
                .map(customer -> mapToResponseDTO(customer, allBookings)) // Gọi hàm map mới có booking list
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponseDTO updateCustomer(Long customerId, CustomerRequestDTO customerRequest) {
        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với ID: " + customerId));

        // Cập nhật thông tin cơ bản
        existingCustomer.setFullName(customerRequest.getFullName());
        existingCustomer.setEmail(customerRequest.getEmail());
        existingCustomer.setIdNumber(customerRequest.getIdNumber());
        existingCustomer.setDateOfBirth(customerRequest.getDateOfBirth());
        existingCustomer.setPhone(customerRequest.getPhone());
        existingCustomer.setAddress(customerRequest.getAddress());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        // Lấy booking để tính lại số liệu (nếu cần cập nhật ngay) hoặc trả về số liệu cũ
        List<Booking> customerBookings = bookingRepository.findByCustomerIdOrderByCheckInDateDesc(customerId);
        return mapToResponseDTO(updatedCustomer, customerBookings);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new EntityNotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
        }
        // Thêm kiểm tra ràng buộc (ví dụ: không xóa nếu có booking đang diễn ra) nếu cần
        customerRepository.deleteById(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomerProfileByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với email: " + email));

        // Dùng hàm mapToResponseDTO (phiên bản không cần list booking, nhanh hơn)
        return mapToResponseDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCustomerIdByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với email: " + email));
        return customer.getId();
    }

    @Override
    @Transactional
    public CustomerResponseDTO updateCustomerProfile(String email, CustomerRequestDTO customerRequest) {
        Customer existingCustomer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với email: " + email));

        // Cập nhật các trường được phép thay đổi
        existingCustomer.setFullName(customerRequest.getFullName());
        existingCustomer.setPhone(customerRequest.getPhone());
        existingCustomer.setAddress(customerRequest.getAddress());
        existingCustomer.setDateOfBirth(customerRequest.getDateOfBirth());
        // Email và CCCD (idNumber) không cho phép thay đổi từ form này

        Customer updatedCustomer = customerRepository.save(existingCustomer);

        return mapToResponseDTO(updatedCustomer);
    }

    @Override
    @Transactional
    public void changeCustomerPassword(String email, String currentPassword, String newPassword) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng."));

        // 1. Kiểm tra mật khẩu hiện tại có đúng không
        if (customer.getPassword() == null || !passwordEncoder.matches(currentPassword, customer.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
        }

        // 2. Kiểm tra mật khẩu mới có khác mật khẩu cũ không
        if (passwordEncoder.matches(newPassword, customer.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu cũ.");
        }

        // 3. Mã hóa và lưu mật khẩu mới
        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
    }
    // --- Hàm tiện ích để chuyển đổi (mapping) ---

    private Customer mapToEntity(CustomerRequestDTO dto) {
        Customer customer = new Customer();
        customer.setFullName(dto.getFullName());
        customer.setIdNumber(dto.getIdNumber());
        customer.setEmail(dto.getEmail());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        // currentPoints và loyaltyTier được set trong createCustomer
        return customer;
    }

    private CustomerResponseDTO mapToResponseDTO(Customer entity, List<Booking> allBookings) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(entity.getId());
        dto.setFullName(entity.getFullName());
        dto.setIdNumber(entity.getIdNumber());
        dto.setEmail(entity.getEmail());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setCreatedAt(entity.getCreatedAt());

        // Thông tin hạng thành viên (giữ nguyên)
        dto.setCurrentPoints(entity.getCurrentPoints());
        if (entity.getLoyaltyTier() != null) {
            dto.setLoyaltyTierId(entity.getLoyaltyTier().getId());
            dto.setLoyaltyTierName(entity.getLoyaltyTier().getName());
            dto.setLoyaltyBenefitsJson(entity.getLoyaltyTier().getBenefitsJson());
        } else {
            dto.setLoyaltyTierId(null); dto.setLoyaltyTierName("N/A"); dto.setLoyaltyBenefitsJson("[]");
        }

        // === TÍNH TOÁN THỐNG KÊ TỪ BOOKING ===
        // Lọc booking chỉ của khách hàng này (nếu đầu vào là allBookings)
        // Nếu đầu vào đã là customerBookings thì không cần lọc lại
        List<Booking> customerBookings = allBookings.stream()
                .filter(b -> b.getCustomer() != null && b.getCustomer().getId().equals(entity.getId()))
                .collect(Collectors.toList());

        // Lọc các booking đã hoàn thành (CHECKED_OUT)
        List<Booking> completedBookings = customerBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CHECKED_OUT)
                .collect(Collectors.toList());

        // Tính toán
        int bookingsCount = completedBookings.size();
        long totalNights = completedBookings.stream()
                .filter(b -> b.getCheckInDate() != null && b.getCheckOutDate() != null) // Đảm bảo ngày hợp lệ
                .mapToLong(b -> ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate()))
                .sum();
        BigDecimal totalSpend = completedBookings.stream()
                .map(b -> b.getTotalPrice() != null ? b.getTotalPrice() : BigDecimal.ZERO) // Xử lý null totalPrice
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Cộng dồn lại

        // Gán vào DTO
        dto.setBookings(bookingsCount);
        dto.setNights(totalNights);
        dto.setSpend(totalSpend);

        return dto;
    }

    // Hàm mapToResponseDTO cũ (không cần List<Booking>) - Dùng khi không cần tính thống kê
    private CustomerResponseDTO mapToResponseDTO(Customer entity) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(entity.getId());
        dto.setFullName(entity.getFullName());
        dto.setIdNumber(entity.getIdNumber());
        dto.setEmail(entity.getEmail());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setCreatedAt(entity.getCreatedAt());

        // Thông tin hạng thành viên
        dto.setCurrentPoints(entity.getCurrentPoints());
        if (entity.getLoyaltyTier() != null) {
            dto.setLoyaltyTierId(entity.getLoyaltyTier().getId());
            dto.setLoyaltyTierName(entity.getLoyaltyTier().getName());
            dto.setLoyaltyBenefitsJson(entity.getLoyaltyTier().getBenefitsJson());
        } else {
            dto.setLoyaltyTierId(null); dto.setLoyaltyTierName("N/A"); dto.setLoyaltyBenefitsJson("[]");
        }

        // Không tính toán thống kê ở đây
        dto.setBookings(0); // Hoặc null
        dto.setNights(0L); // Hoặc null
        dto.setSpend(BigDecimal.ZERO); // Hoặc null

        return dto;
    }
}