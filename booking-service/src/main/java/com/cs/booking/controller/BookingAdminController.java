package com.cs.booking.controller;

import com.cs.booking.entity.Booking;
import com.cs.booking.repository.BookingRepository;
import com.cs.common.constant.AuthConstant;
import com.cs.common.dto.ApiResponse;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bookings")
public class BookingAdminController {

    private final BookingRepository bookingRepository;

    public BookingAdminController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    private void checkAdmin(String role) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("无管理员权限");
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String status,
            @RequestHeader(AuthConstant.HEADER_USER_ROLE) String role) {
        checkAdmin(role);

        Page<Booking> result;
        if (userName != null && !userName.isEmpty() && status != null && !status.isEmpty()) {
            result = bookingRepository.findByUserNameContainingAndStatusOrderByCreatedAtDesc(
                    userName, Booking.BookingStatus.valueOf(status),
                    PageRequest.of(page, size));
        } else if (userName != null && !userName.isEmpty()) {
            result = bookingRepository.findByUserNameContainingOrderByCreatedAtDesc(
                    userName, PageRequest.of(page, size));
        } else if (status != null && !status.isEmpty()) {
            result = bookingRepository.findByStatusOrderByCreatedAtDesc(
                    Booking.BookingStatus.valueOf(status), PageRequest.of(page, size));
        } else {
            result = bookingRepository.findAllByOrderByCreatedAtDesc(
                    PageRequest.of(page, size));
        }

        return ApiResponse.ok(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "currentPage", result.getNumber()
        ));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Booking> updateStatus(@PathVariable Long id,
                                              @RequestParam String status,
                                              @RequestHeader(AuthConstant.HEADER_USER_ROLE) String role) {
        checkAdmin(role);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        booking.setStatus(Booking.BookingStatus.valueOf(status));
        bookingRepository.save(booking);
        return ApiResponse.ok(booking);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id,
                                       @RequestHeader(AuthConstant.HEADER_USER_ROLE) String role) {
        checkAdmin(role);
        bookingRepository.deleteById(id);
        return ApiResponse.ok("ok");
    }
}
