package com.cs.booking.service;

import com.cs.booking.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BookingTimeoutConsumer {

    private final BookingService bookingService;

    public BookingTimeoutConsumer(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @RabbitListener(queues = RabbitMQConfig.BOOKING_PROCESS_QUEUE)
    public void handleTimeout(Long bookingId) {
        bookingService.autoCancel(bookingId);
    }
}
