package com.example.hotel.service.impl;

import com.example.hotel.dto.BookingDto;
import com.example.hotel.dto.BookingHistoryDto;
import com.example.hotel.entity.Booking;
import com.example.hotel.entity.BookingStatus;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.Customer;
import com.example.hotel.entity.RoomStatus; // <-- ĐẢM BẢO BẠN CÓ DÒNG NÀY
import com.example.hotel.repository.CustomerRepository;
import com.example.hotel.service.LoyaltyAutomationService;
import lombok.extern.slf4j.Slf4j;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.repository.BookingRepository;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.service.BookingService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // <-- Đảm bảo bạn có @Slf4j để sử dụng log
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final LoyaltyAutomationService loyaltyAutomationService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> findBookings(String status, LocalDate fromDate, LocalDate toDate, Long roomTypeId, String search, Pageable pageable) {
        Specification<Booking> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isEmpty()) {
                String snakeCaseStatus = status.replace('-', '_').toUpperCase();
                predicates.add(cb.equal(root.get("status"), BookingStatus.valueOf(snakeCaseStatus)));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkOutDate"), toDate));
            }
            if (roomTypeId != null) {
                predicates.add(cb.equal(root.get("room").get("roomType").get("id"), roomTypeId));
            }
            if (search != null && !search.isEmpty()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("customerFullName")), likePattern),
                        cb.like(cb.lower(root.get("customerPhone")), likePattern),
                        cb.like(cb.lower(root.get("bookingConfirmationCode")), likePattern),
                        cb.like(cb.lower(root.get("room").get("roomNumber")), likePattern)
                ));
            }

            if (search != null && search.matches("\\d+")) { // Nếu search là số
                predicates.add(cb.or(
                        // ... (các điều kiện cũ)
                        cb.equal(root.get("customer").get("id"), Long.parseLong(search))
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Booking> bookings = bookingRepository.findAll(spec, pageable);
        return bookings.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return convertToDto(booking);
    }

    @Override
    @Transactional
    public BookingDto createBooking(BookingDto bookingDto) {
        Booking booking = convertToEntity(bookingDto);

        Room room = booking.getRoom();
        if (room == null) {
            throw new ResourceNotFoundException("Không tìm thấy phòng với ID: " + bookingDto.getRoomId());
        }
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new IllegalStateException("Phòng " + room.getRoomNumber() + " không ở trạng thái 'Trống' (AVAILABLE).");
        }

        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingConfirmationCode("#BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        Booking savedBooking = bookingRepository.save(booking);
        return convertToDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto updateBooking(Long id, BookingDto bookingDto) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: "+ id));
        updateEntityFromDto(booking, bookingDto);
        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDto(updatedBooking);
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (booking.getStatus() != BookingStatus.CHECKED_OUT && booking.getRoom() != null) {
            RoomStatus roomStatus = booking.getRoom().getStatus();

            if(roomStatus == RoomStatus.RESERVED || roomStatus == RoomStatus.OCCUPIED) {
                Room room = booking.getRoom();
                room.setStatus(RoomStatus.AVAILABLE);
                roomRepository.save(room);
                log.info("Booking {} bị xóa, đã trả phòng {} về AVAILABLE.", id, room.getId());
            }
        }

        bookingRepository.deleteById(id);
    }


    /**
     * === HÀM GỠ LỖI (DEBUGGING) ===
     */
    @Override
    @Transactional
    public BookingDto updateBookingStatus(Long id, String status) {
        // --- LOG BẮT ĐẦU ---
        log.info("--- BẮT ĐẦU updateBookingStatus ---");
        log.info("Booking ID: {}, Yêu cầu đổi trạng thái thành: {}", id, status);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        Room room = booking.getRoom();

        // --- LOG KIỂM TRA 1: PHÒNG CÓ TỒN TẠI KHÔNG? ---
        if (room == null) {
            log.error("LỖI NGHIÊM TRỌNG: Booking {} không tìm thấy Room liên kết! Logic đồng bộ hóa thất bại.", id);
            // Dừng lại ở đây, vì logic dưới sẽ vô dụng
        } else {
            log.info("KIỂM TRA 1: Tốt. Tìm thấy Phòng liên kết. ID Phòng: {}, Trạng thái HIỆN TẠI của phòng: {}", room.getId(), room.getStatus().name());
        }

        BookingStatus newStatus; // Trạng thái mới của Booking
        try {
            newStatus = BookingStatus.valueOf(status.replace('-', '_').toUpperCase());
            log.info("Đã chuyển đổi trạng thái booking thành Enum: {}", newStatus.name());
        } catch (IllegalArgumentException e) {
            log.error("Lỗi: Trạng thái booking không hợp lệ: {}", status);
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }

        if (room != null) {
            // Xác định trạng thái mới của Phòng
            RoomStatus newRoomStatus = room.getStatus(); // Mặc định là trạng thái cũ (Enum)
            log.info("Trạng thái phòng ban đầu là: {}", newRoomStatus.name());

            switch (newStatus) { // (switch theo trạng thái Booking)
                case CONFIRMED:
                    newRoomStatus = RoomStatus.RESERVED;
                    log.info("Logic: Booking CONFIRMED -> Phòng sẽ là RESERVED");
                    break;
                case CHECKED_IN:
                    newRoomStatus = RoomStatus.OCCUPIED;
                    log.info("Logic: Booking CHECKED_IN -> Phòng sẽ là OCCUPIED");
                    break;
                case CHECKED_OUT:
                    newRoomStatus = RoomStatus.AVAILABLE;
                    log.info("Logic: Booking CHECKED_OUT -> Phòng sẽ là AVAILABLE");
                    break;
                case CANCELLED:
                    newRoomStatus = RoomStatus.AVAILABLE;
                    log.info("Logic: Booking CANCELLED -> Phòng sẽ là AVAILABLE");
                    break;
                case PENDING:
                    if (room.getStatus() == RoomStatus.RESERVED) {
                        newRoomStatus = RoomStatus.AVAILABLE;
                        log.info("Logic: Booking PENDING (từ RESERVED) -> Phòng sẽ là AVAILABLE");
                    } else {
                        log.info("Logic: Booking PENDING (từ trạng thái khác) -> Phòng giữ nguyên");
                    }
                    break;
            }

            // --- LOG KIỂM TRA 2: CÓ LƯU PHÒNG KHÔNG? ---
            if (room.getStatus() != newRoomStatus) {
                log.info("PHÁT HIỆN THAY ĐỔI: Trạng thái phòng cũ ({}) != Trạng thái phòng mới ({}). Đang tiến hành lưu...", room.getStatus().name(), newRoomStatus.name());
                room.setStatus(newRoomStatus);
                roomRepository.save(room); // <--- LƯU PHÒNG
                log.info("!!! THÀNH CÔNG: Đã CẬP NHẬT TRẠNG THÁI PHÒNG {} thành {}", room.getId(), newRoomStatus.name());
            } else {
                log.info("KHÔNG THAY ĐỔI: Trạng thái phòng cũ ({}) == Trạng thái phòng mới ({}). Không cần lưu.", room.getStatus().name(), newRoomStatus.name());
            }
        }

        // Cập nhật thời gian check-in/check-out thực tế
        if (newStatus == BookingStatus.CHECKED_IN) {
            if (booking.getActualCheckinTime() == null) {
                booking.setActualCheckinTime(LocalDateTime.now());
                log.info("Đã lưu thời gian check-in thực tế.");
            }
        } else if (newStatus == BookingStatus.CHECKED_OUT) {
            if (booking.getActualCheckoutTime() == null) {
                booking.setActualCheckoutTime(LocalDateTime.now());
                log.info("Đã lưu thời gian check-out thực tế.");
            }
        }

        // Cập nhật và lưu trạng thái ĐẶT PHÒNG
        log.info("Đang cập nhật trạng thái Booking...");
        booking.setStatus(newStatus);
        Booking updatedBooking = bookingRepository.save(booking); // <--- LƯU BOOKING
        log.info("Đã cập nhật Booking. Trạng thái mới: {}", updatedBooking.getStatus().name());

        // Xử lý điểm thưởng
        if (newStatus == BookingStatus.CHECKED_OUT) {
            log.info("Booking {} đã CHECKED_OUT, bắt đầu xử lý điểm thưởng.", id);
            try {
                loyaltyAutomationService.processBookingCheckout(updatedBooking);
            } catch (Exception e) {
                log.error("Lỗi nghiêm trọng khi xử lý điểm thưởng cho booking {}: {}", id, e.getMessage());
            }
        }

        log.info("--- KẾT THÚC updateBookingStatus ---");
        return convertToDto(updatedBooking);
    }
    // === KẾT THÚC HÀM GỠ LỖI ===


    @Override
    @Transactional(readOnly = true)
    public List<BookingHistoryDto> getBookingHistoryByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
        }
        List<Booking> bookings = bookingRepository.findByCustomerIdOrderByCheckInDateDesc(customerId);
        return bookings.stream()
                .map(this::mapBookingToHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> getDeletedBookings(Pageable pageable) {
        log.info("Đang lấy danh sách booking trong thùng rác...");
        Page<Booking> deletedBookings = bookingRepository.findAllDeleted(pageable);
        return deletedBookings.map(this::convertToDto);
    }

    private BookingHistoryDto mapBookingToHistoryDto(Booking booking) {
        BookingHistoryDto dto = new BookingHistoryDto();
        dto.setBookingCode(booking.getBookingConfirmationCode());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus().name());

        if (booking.getRoom() != null) {
            dto.setRoomNumber(booking.getRoom().getRoomNumber());
            if (booking.getRoom().getRoomType() != null) {
                dto.setRoomTypeName(booking.getRoom().getRoomType().getName());
            } else {
                dto.setRoomTypeName("N/A");
            }
        } else {
            dto.setRoomNumber("N/A");
            dto.setRoomTypeName("N/A");
        }
        return dto;
    }

    private BookingDto convertToDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setCode(booking.getBookingConfirmationCode());
        if (booking.getCreatedDate() != null) {
            dto.setCreated(booking.getCreatedDate().format(dateTimeFormatter));
        }
        dto.setStatus(booking.getStatus().name().toLowerCase().replace("_", "-"));
        dto.setCustomer(booking.getCustomerFullName());
        dto.setPhone(booking.getCustomerPhone());

        if (booking.getCustomer() != null) {
            dto.setCustomerId(booking.getCustomer().getId());
        }

        if (booking.getRoom() != null) {
            dto.setRoomId(booking.getRoom().getId());
            dto.setRoomNumber(booking.getRoom().getRoomNumber());
            // === SỬA LỖI POTENTIAL NULL POINTER ===
            // Thêm kiểm tra null cho getRoomType()
            if (booking.getRoom().getRoomType() != null) {
                dto.setRoomType(booking.getRoom().getRoomType().getName());
            } else {
                dto.setRoomType("N/A (Loại phòng không xác định)");
            }
        }
        dto.setCheckin(booking.getCheckInDate().format(dateFormatter));
        dto.setCheckout(booking.getCheckOutDate().format(dateFormatter));
        dto.setNights(ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate()));
        dto.setPricePerNight(booking.getPricePerNight());
        dto.setTotal(booking.getTotalPrice());

        if (booking.getActualCheckinTime() != null) {
            dto.setActualCheckinTime(booking.getActualCheckinTime().format(dateTimeFormatter));
        }
        if (booking.getActualCheckoutTime() != null) {
            dto.setActualCheckoutTime(booking.getActualCheckoutTime().format(dateTimeFormatter));
        }

        dto.setSoNguoiLon(booking.getSoNguoiLon());
        dto.setSoTreEm(booking.getSoTreEm());

        return dto;
    }

    private Booking convertToEntity(BookingDto dto) {
        Booking booking = new Booking();

        if (dto.getCustomerId() == null) {
            throw new IllegalArgumentException("CustomerId không được để trống khi tạo đặt phòng");
        }
        if (dto.getRoomId() == null) {
            throw new IllegalArgumentException("RoomId không được để trống khi tạo đặt phòng");
        }

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + dto.getCustomerId()));

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + dto.getRoomId()));

        booking.setCustomer(customer);
        booking.setRoom(room);

        booking.setCustomerFullName(dto.getCustomer() != null ? dto.getCustomer() : customer.getFullName());
        booking.setCustomerPhone(dto.getPhone() != null ? dto.getPhone() : customer.getPhone());

        booking.setCheckInDate(LocalDate.parse(dto.getCheckin(), dateFormatter));
        booking.setCheckOutDate(LocalDate.parse(dto.getCheckout(), dateFormatter));
        booking.setTotalPrice(dto.getTotal());

        // === SỬA LỖI: Lấy giá từ RoomType thay vì Room ===
        // Giá (price) đã được thêm vào Room entity, nên dùng room.getPrice() là ĐÚNG
        if (room.getPrice() == null) {
            log.warn("Phòng {} không có giá (price). Đặt giá tạm thời là 0.", room.getId());
            booking.setPricePerNight(BigDecimal.ZERO);
        } else {
            booking.setPricePerNight(room.getPrice());
        }

        booking.setSoNguoiLon(dto.getSoNguoiLon() > 0 ? dto.getSoNguoiLon() : 1);
        booking.setSoTreEm(dto.getSoTreEm());

        return booking;
    }

    private Booking updateEntityFromDto(Booking booking, BookingDto dto) {

        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + dto.getCustomerId()));
            booking.setCustomer(customer);
            booking.setCustomerFullName(dto.getCustomer() != null ? dto.getCustomer() : customer.getFullName());
            booking.setCustomerPhone(dto.getPhone() != null ? dto.getPhone() : customer.getPhone());
        }

        if (dto.getRoomId() != null) {
            Room room = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + dto.getRoomId()));
            booking.setRoom(room);
            // === SỬA LỖI: Cập nhật PricePerNight khi phòng thay đổi ===
            if (room.getPrice() == null) {
                log.warn("Phòng {} không có giá (price). Đặt giá tạm thời là 0.", room.getId());
                booking.setPricePerNight(BigDecimal.ZERO);
            } else {
                booking.setPricePerNight(room.getPrice());
            }
        }

        booking.setCheckInDate(LocalDate.parse(dto.getCheckin(), dateFormatter));
        booking.setCheckOutDate(LocalDate.parse(dto.getCheckout(), dateFormatter));
        booking.setTotalPrice(dto.getTotal());

        booking.setSoNguoiLon(dto.getSoNguoiLon() > 0 ? dto.getSoNguoiLon() : 1);
        booking.setSoTreEm(dto.getSoTreEm());

        return booking;
    }
}