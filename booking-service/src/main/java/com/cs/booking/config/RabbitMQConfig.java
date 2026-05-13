package com.cs.booking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BOOKING_EXCHANGE = "booking.exchange";
    public static final String BOOKING_DELAY_QUEUE = "booking.delay.queue";
    public static final String BOOKING_DELAY_ROUTING_KEY = "booking.delay";
    public static final String BOOKING_PROCESS_QUEUE = "booking.process.queue";
    public static final String BOOKING_PROCESS_ROUTING_KEY = "booking.process";

    @Bean
    public DirectExchange bookingExchange() {
        return new DirectExchange(BOOKING_EXCHANGE);
    }

    @Bean
    public Queue bookingDelayQueue() {
        return QueueBuilder.durable(BOOKING_DELAY_QUEUE)
                .ttl(15 * 60 * 1000)
                .deadLetterExchange(BOOKING_EXCHANGE)
                .deadLetterRoutingKey(BOOKING_PROCESS_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(bookingDelayQueue())
                .to(bookingExchange())
                .with(BOOKING_DELAY_ROUTING_KEY);
    }

    @Bean
    public Queue bookingProcessQueue() {
        return QueueBuilder.durable(BOOKING_PROCESS_QUEUE).build();
    }

    @Bean
    public Binding processBinding() {
        return BindingBuilder.bind(bookingProcessQueue())
                .to(bookingExchange())
                .with(BOOKING_PROCESS_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
