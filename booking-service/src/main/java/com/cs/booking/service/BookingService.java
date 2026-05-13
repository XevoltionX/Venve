package com.cs.booking.service;

import com.cs.booking.config.RabbitMQConfig;
import com.cs.booking.entity.Booking;
import com.cs.booking.repository.BookingRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;
    private final NotificationService notificationService;

    public BookingService(BookingRepository bookingRepository,
                          RedissonClient redissonClient,
                          RabbitTemplate rabbitTemplate,
                          NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.redissonClient = redissonClient;
        this.rabbitTemplate = rabbitTemplate;
        this.notificationService = notificationService;
    }

    @Transactional
    public Booking create(Long userId, String userName, Long venueId,
                          LocalDate date, LocalTime start, LocalTime end) {
        String lockKey = "booking-lock:" + venueId + ":" + date + ":" + start + ":" + end;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                throw new RuntimeException("当前时段正在被其他人预约，请稍后再试");
            }

            boolean conflict = bookingRepository
                    .existsByVenueIdAndBookingDateAndStartTimeAndEndTimeAndStatusNot(
                            venueId, date, start, end, Booking.BookingStatus.CANCELLED);
            if (conflict) {
                throw new RuntimeException("该时段已被预约");
            }

            Booking booking = new Booking();
            booking.setUserId(userId);
            booking.setUserName(userName);
            booking.setVenueId(venueId);
            booking.setBookingDate(date);
            booking.setStartTime(start);
            booking.setEndTime(end);
            booking.setStatus(Booking.BookingStatus.PENDING);
            bookingRepository.save(booking);

            rabbitTemplate.convertAndSend(RabbitMQConfig.BOOKING_EXCHANGE,
                    RabbitMQConfig.BOOKING_DELAY_ROUTING_KEY, booking.getId());

            notificationService.send(userId, "BOOKING_CREATED",
                    "预约已创建",
                    "球台 #" + venueId + " " + date + " " + start + "-" + end + " 预约成功，请尽快确认。");

            return booking;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("预约被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public List<Booking> listByUser(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void confirm(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此预约");
        }
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("当前状态不允许确认");
        }
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        notificationService.send(userId, "BOOKING_CONFIRMED",
                "预约已确认",
                "球台 #" + booking.getVenueId() + " " + booking.getBookingDate() + " 预约已确认。");
    }

    @Transactional
    public void cancel(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此预约");
        }
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("预约已被取消");
        }
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        notificationService.send(userId, "BOOKING_CANCELLED",
                "预约已取消",
                "球台 #" + booking.getVenueId() + " " + booking.getBookingDate() + " 预约已取消。");
    }

    @Transactional
    public void autoCancel(Long bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if (booking.getStatus() == Booking.BookingStatus.PENDING) {
                booking.setStatus(Booking.BookingStatus.CANCELLED);
                bookingRepository.save(booking);

                notificationService.send(booking.getUserId(), "BOOKING_TIMEOUT",
                        "预约已超时取消",
                        "球台 #" + booking.getVenueId() + " " + booking.getBookingDate()
                                + " " + booking.getStartTime() + "-" + booking.getEndTime()
                                + " 超时未确认，已自动取消。");
            }
        });
    }

    public List<Slot> getAvailableSlots(Long venueId, LocalDate date) {
        List<Slot> allSlots = generateSlots();
        List<Slot> available = new ArrayList<>();
        for (Slot slot : allSlots) {
            boolean booked = bookingRepository
                    .existsByVenueIdAndBookingDateAndStartTimeAndEndTimeAndStatusNot(
                            venueId, date, slot.start, slot.end, Booking.BookingStatus.CANCELLED);
            if (!booked) {
                available.add(slot);
            }
        }
        return available;
    }

    private List<Slot> generateSlots() {
        List<Slot> slots = new ArrayList<>();
        for (int hour = 9; hour < 21; hour++) {
            slots.add(new Slot(LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0)));
        }
        return slots;
    }

    public record Slot(LocalTime start, LocalTime end) {}
}
