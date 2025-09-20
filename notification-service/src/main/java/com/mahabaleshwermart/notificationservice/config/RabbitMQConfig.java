package com.mahabaleshwermart.notificationservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Notification Service
 * Auto-declares queues that this service consumes from
 */
@Configuration
public class RabbitMQConfig {

    /**
     * Order confirmation queue
     */
    @Bean
    public Queue orderConfirmedQueue() {
        return QueueBuilder.durable("order.confirmed.queue").build();
    }

    /**
     * Order status update queue
     */
    @Bean
    public Queue orderStatusUpdatedQueue() {
        return QueueBuilder.durable("order.status.updated.queue").build();
    }

    /**
     * Order cancellation queue
     */
    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable("order.cancelled.queue").build();
    }

    /**
     * Payment confirmation queue
     */
    @Bean
    public Queue paymentConfirmedQueue() {
        return QueueBuilder.durable("payment.confirmed.queue").build();
    }

    /**
     * Order delivered queue
     */
    @Bean
    public Queue orderDeliveredQueue() {
        return QueueBuilder.durable("order.delivered.queue").build();
    }
}
