package com.mahabaleshwermart.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Notification Service
 * Auto-declares queues that this service consumes from
 */
@Configuration
public class RabbitMQConfig {

    private static final String ORDER_NOTIFICATION_EXCHANGE = "order.notifications";
    private static final String RK_ORDER_CONFIRMED = "order.confirmed";
    private static final String RK_ORDER_STATUS_UPDATED = "order.status.updated";
    private static final String RK_ORDER_CANCELLED = "order.cancelled";
    private static final String RK_PAYMENT_CONFIRMED = "payment.confirmed";
    private static final String RK_ORDER_DELIVERED = "order.delivered";

    @Bean
    public TopicExchange orderNotificationsExchange() {
        return new TopicExchange(ORDER_NOTIFICATION_EXCHANGE, true, false);
    }

    /**
     * Order confirmation queue
     */
    @Bean
    public Queue orderConfirmedQueue() {
        return QueueBuilder.durable("order.confirmed.queue").build();
    }

    @Bean
    public Binding bindOrderConfirmed(Queue orderConfirmedQueue, TopicExchange orderNotificationsExchange) {
        return BindingBuilder.bind(orderConfirmedQueue).to(orderNotificationsExchange).with(RK_ORDER_CONFIRMED);
    }

    /**
     * Order status update queue
     */
    @Bean
    public Queue orderStatusUpdatedQueue() {
        return QueueBuilder.durable("order.status.updated.queue").build();
    }

    @Bean
    public Binding bindOrderStatusUpdated(Queue orderStatusUpdatedQueue, TopicExchange orderNotificationsExchange) {
        return BindingBuilder.bind(orderStatusUpdatedQueue).to(orderNotificationsExchange).with(RK_ORDER_STATUS_UPDATED);
    }

    /**
     * Order cancellation queue
     */
    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable("order.cancelled.queue").build();
    }

    @Bean
    public Binding bindOrderCancelled(Queue orderCancelledQueue, TopicExchange orderNotificationsExchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(orderNotificationsExchange).with(RK_ORDER_CANCELLED);
    }

    /**
     * Payment confirmation queue
     */
    @Bean
    public Queue paymentConfirmedQueue() {
        return QueueBuilder.durable("payment.confirmed.queue").build();
    }

    @Bean
    public Binding bindPaymentConfirmed(Queue paymentConfirmedQueue, TopicExchange orderNotificationsExchange) {
        return BindingBuilder.bind(paymentConfirmedQueue).to(orderNotificationsExchange).with(RK_PAYMENT_CONFIRMED);
    }

    /**
     * Order delivered queue
     */
    @Bean
    public Queue orderDeliveredQueue() {
        return QueueBuilder.durable("order.delivered.queue").build();
    }

    @Bean
    public Binding bindOrderDelivered(Queue orderDeliveredQueue, TopicExchange orderNotificationsExchange) {
        return BindingBuilder.bind(orderDeliveredQueue).to(orderNotificationsExchange).with(RK_ORDER_DELIVERED);
    }
}
