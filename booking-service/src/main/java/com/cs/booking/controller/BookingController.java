package com.cs.booking.controller;

import com.cs.booking.dto.BookingRequest;
import com.cs.booking.entity.Booking;
import com.cs.booking.service.BookingService;
import com.cs.common.constant.AuthConstant;
import com.cs.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    public ApiResponse<Booking> create(@Valid @RequestBody BookingRequest req,
                                        @RequestHeader(AuthConstant.HEADER_USER_ID) Long userId,
                                        @RequestHeader(AuthConstant.HEADER_USER_NAME) String userName) {
        return ApiResponse.ok(bookingService.create(userId, userName,
                req.getVenueId(), req.getBookingDate(), req.getStartTime(), req.getEndTime()));
    }

    @GetMapping("/bookings")
    public ApiResponse<List<Booking>> list(@RequestHeader(AuthConstant.HEADER_USER_ID) Long userId) {
        return ApiResponse.ok(bookingService.listByUser(userId));
    }

    @PutMapping("/bookings/{id}/confirm")
    public ApiResponse<String> confirm(@PathVariable Long id,
                                        @RequestHeader(AuthConstant.HEADER_USER_ID) Long userId) {
        bookingService.confirm(id, userId);
        return ApiResponse.ok("预约已确认");
    }

    @PutMapping("/bookings/{id}/cancel")
    public ApiResponse<String> cancel(@PathVariable Long id,
                                       @RequestHeader(AuthConstant.HEADER_USER_ID) Long userId) {
        bookingService.cancel(id, userId);
        return ApiResponse.ok("预约已取消");
    }

    @GetMapping("/bookings/slots/{venueId}")
    public ApiResponse<List<BookingService.Slot>> getSlots(@PathVariable Long venueId,
                                                            @RequestParam LocalDate date) {
        return ApiResponse.ok(bookingService.getAvailableSlots(venueId, date));
    }
}
