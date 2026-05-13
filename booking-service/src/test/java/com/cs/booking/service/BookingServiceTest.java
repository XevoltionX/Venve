package com.cs.booking.service;

import com.cs.booking.entity.Booking;
import com.cs.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private RedissonClient redissonClient;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private NotificationService notificationService;
    @Mock private RLock rLock;

    @InjectMocks
    private BookingService bookingService;

    private final Long userId = 1L;
    private final Long venueId = 1L;
    private final LocalDate date = LocalDate.now().plusDays(1);
    private final LocalTime start = LocalTime.of(10, 0);
    private final LocalTime end = LocalTime.of(11, 0);

    private void mockLock() throws InterruptedException {
        lenient().when(redissonClient.getLock(anyString())).thenReturn(rLock);
        lenient().when(rLock.tryLock(5, 10, TimeUnit.SECONDS)).thenReturn(true);
        lenient().when(rLock.isHeldByCurrentThread()).thenReturn(true);
    }

    @Test
    void shouldCreateBookingWithLock() throws InterruptedException {
        mockLock();
        when(bookingRepository.existsByVenueIdAndBookingDateAndStartTimeAndEndTimeAndStatusNot(
                eq(venueId), eq(date), eq(start), eq(end), eq(Booking.BookingStatus.CANCELLED)))
                .thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        Booking result = bookingService.create(userId, "test", venueId, date, start, end);

        assertNotNull(result);
        assertEquals(Booking.BookingStatus.PENDING, result.getStatus());
        assertEquals(userId, result.getUserId());
        verify(bookingRepository).save(any(Booking.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), eq(1L));
        verify(notificationService).send(eq(userId), eq("BOOKING_CREATED"), anyString(), anyString());
    }

    @Test
    void shouldRejectDuplicateBooking() throws InterruptedException {
        mockLock();
        when(bookingRepository.existsByVenueIdAndBookingDateAndStartTimeAndEndTimeAndStatusNot(
                eq(venueId), eq(date), eq(start), eq(end), eq(Booking.BookingStatus.CANCELLED)))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bookingService.create(userId, "test", venueId, date, start, end));
        assertTrue(ex.getMessage().contains("已被预约"));
    }

    @Test
    void shouldConfirmBooking() {
        Booking booking = new Booking();
        booking.setId(1L); booking.setUserId(userId);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setVenueId(venueId); booking.setBookingDate(date);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.confirm(1L, userId);

        assertEquals(Booking.BookingStatus.CONFIRMED, booking.getStatus());
        verify(notificationService).send(eq(userId), eq("BOOKING_CONFIRMED"), anyString(), anyString());
    }

    @Test
    void shouldRejectConfirmByOtherUser() {
        Booking booking = new Booking();
        booking.setId(1L); booking.setUserId(99L);
        booking.setStatus(Booking.BookingStatus.PENDING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bookingService.confirm(1L, userId));
        assertTrue(ex.getMessage().contains("无权"));
    }

    @Test
    void shouldAutoCancelPendingBooking() {
        Booking booking = new Booking();
        booking.setId(1L); booking.setUserId(userId);
        booking.setVenueId(venueId); booking.setBookingDate(date);
        booking.setStartTime(start); booking.setEndTime(end);
        booking.setStatus(Booking.BookingStatus.PENDING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.autoCancel(1L);

        assertEquals(Booking.BookingStatus.CANCELLED, booking.getStatus());
        verify(notificationService).send(eq(userId), eq("BOOKING_TIMEOUT"), anyString(), anyString());
    }

    @Test
    void shouldReturnAvailableSlots() {
        when(bookingRepository.existsByVenueIdAndBookingDateAndStartTimeAndEndTimeAndStatusNot(
                anyLong(), any(), any(), any(), any()))
                .thenReturn(false);

        List<BookingService.Slot> slots = bookingService.getAvailableSlots(venueId, date);

        assertEquals(12, slots.size()); // 09:00-21:00
        assertEquals(LocalTime.of(9, 0), slots.get(0).start());
    }

    @Test
    void shouldNotReturnBookedSlots() {
        // Only the 10:00-11:00 slot is booked
        when(bookingRepository.existsByVenueIdAndBookingDateAndStartTimeAndEndTimeAndStatusNot(
                anyLong(), any(), any(), any(), any()))
                .thenReturn(false);
        when(bookingRepository.existsByVenueIdAndBookingDateAndStartTimeAndEndTimeAndStatusNot(
                eq(venueId), eq(date),
                eq(LocalTime.of(10, 0)), eq(LocalTime.of(11, 0)),
                eq(Booking.BookingStatus.CANCELLED)))
                .thenReturn(true);

        List<BookingService.Slot> slots = bookingService.getAvailableSlots(venueId, date);
        assertEquals(11, slots.size()); // 12 - 1
    }
}
