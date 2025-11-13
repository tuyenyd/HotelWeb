package com.example.hotel.controller;

import com.example.hotel.dto.BookingDto;
import com.example.hotel.dto.PaymentDto;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingAdminController {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<Page<BookingDto>> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "createdDate,desc") String[] sort) {

        String sortField = sort[0];
        Sort.Direction sortDirection = (sort.length > 1 && sort[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        Page<BookingDto> bookingPage = bookingService.findBookings(status, fromDate, toDate, roomTypeId, search, pageable);
        return ResponseEntity.ok(bookingPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@RequestBody BookingDto bookingDto) {
        BookingDto createdBooking = bookingService.createBooking(bookingDto);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDto> updateBooking(@PathVariable Long id, @RequestBody BookingDto bookingDto) {
        return ResponseEntity.ok(bookingService.updateBooking(id, bookingDto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingDto> updateBookingStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("status");
        if (status == null || status.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trash")
    public ResponseEntity<Page<BookingDto>> getTrashBookings(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "createdDate,desc") String[] sort) {

        String sortField = sort[0];
        if (sortField.equals("createdDate")) {
            sortField = "created_date";
        }
        Sort.Direction sortDirection = (sort.length > 1 && sort[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<BookingDto> trashPage = bookingService.getDeletedBookings(pageable);
        return ResponseEntity.ok(trashPage);
    }
    @GetMapping("/{id}/payments")
    public ResponseEntity<List<PaymentDto>> getBookingPayments(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentsForBooking(id));
    }
}
