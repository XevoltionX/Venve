package com.cs.booking.repository;

import com.cs.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByVenueIdAndBookingDateAndStartTimeAndEndTimeAndStatusNot(
            Long venueId, LocalDate date, LocalTime start, LocalTime end, Booking.BookingStatus status);

    // Admin paginated queries
    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Booking> findByUserNameContainingOrderByCreatedAtDesc(String userName, Pageable pageable);

    Page<Booking> findByStatusOrderByCreatedAtDesc(Booking.BookingStatus status, Pageable pageable);

    Page<Booking> findByUserNameContainingAndStatusOrderByCreatedAtDesc(
            String userName, Booking.BookingStatus status, Pageable pageable);
}
