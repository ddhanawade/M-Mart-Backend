package com.mahabaleshwermart.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order notification event for RabbitMQ messaging
 * Shared across order-service and notification-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotificationEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String eventType;
    private String orderId;
    private String orderNumber;
    private String userEmail;
    private String userName;
    private String userPhone;
    private BigDecimal totalAmount;
    private String message;
    private String oldStatus;
    private String newStatus;
    private String trackingNumber;
    private String transactionId;
    private String cancellationReason;
    private String deliveryAddress;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
