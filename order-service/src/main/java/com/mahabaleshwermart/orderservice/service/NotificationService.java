package com.mahabaleshwermart.orderservice.service;

import com.mahabaleshwermart.common.events.OrderNotificationEvent;
import com.mahabaleshwermart.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Notification Service
 * Handles sending notifications for order events via Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    // Kafka Topic Names
    private static final String ORDER_CONFIRMED_TOPIC = "order-confirmed";
    private static final String ORDER_STATUS_UPDATED_TOPIC = "order-status-updated";
    private static final String ORDER_CANCELLED_TOPIC = "order-cancelled";
    private static final String PAYMENT_CONFIRMED_TOPIC = "payment-confirmed";
    private static final String ORDER_DELIVERED_TOPIC = "order-delivered";
    
    /**
     * Send order confirmation notification
     */
    public void sendOrderConfirmation(Order order) {
        log.info("Sending order confirmation notification for order: {}", order.getOrderNumber());
        
        try {
            OrderNotificationEvent event = OrderNotificationEvent.builder()
                    .eventType("ORDER_CONFIRMED")
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .userEmail(order.getUserEmail())
                    .userName(order.getUserName())
                    .userPhone(order.getUserPhone())
                    .totalAmount(order.getTotalAmount())
                    .message("Your order has been confirmed successfully!")
                    .build();
            
            kafkaTemplate.send(ORDER_CONFIRMED_TOPIC, order.getOrderNumber(), event);
            log.info("Order confirmation notification sent successfully to Kafka topic: {}", ORDER_CONFIRMED_TOPIC);
            
        } catch (Exception e) {
            log.error("Failed to send order confirmation notification for order: {}", order.getOrderNumber(), e);
        }
    }
    
    /**
     * Send order status update notification
     */
    public void sendOrderStatusUpdate(Order order, Order.OrderStatus oldStatus, Order.OrderStatus newStatus) {
        log.info("Sending order status update notification for order: {} from {} to {}", 
                order.getOrderNumber(), oldStatus, newStatus);
        
        try {
            OrderNotificationEvent event = OrderNotificationEvent.builder()
                    .eventType("ORDER_STATUS_UPDATED")
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .userEmail(order.getUserEmail())
                    .userName(order.getUserName())
                    .userPhone(order.getUserPhone())
                    .oldStatus(oldStatus.name())
                    .newStatus(newStatus.name())
                    .message(getStatusUpdateMessage(newStatus))
                    .trackingNumber(order.getTrackingNumber())
                    .build();
            
            kafkaTemplate.send(ORDER_STATUS_UPDATED_TOPIC, order.getOrderNumber(), event);
            log.info("Order status update notification sent successfully to Kafka topic: {}", ORDER_STATUS_UPDATED_TOPIC);
            
        } catch (Exception e) {
            log.error("Failed to send order status update notification for order: {}", order.getOrderNumber(), e);
        }
    }
    
    /**
     * Send order cancellation notification
     */
    public void sendOrderCancellation(Order order, String reason) {
        log.info("Sending order cancellation notification for order: {}", order.getOrderNumber());
        
        try {
            OrderNotificationEvent event = OrderNotificationEvent.builder()
                    .eventType("ORDER_CANCELLED")
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .userEmail(order.getUserEmail())
                    .userName(order.getUserName())
                    .userPhone(order.getUserPhone())
                    .totalAmount(order.getTotalAmount())
                    .message("Your order has been cancelled. " + (reason != null ? "Reason: " + reason : ""))
                    .cancellationReason(reason)
                    .build();
            
            kafkaTemplate.send(ORDER_CANCELLED_TOPIC, order.getOrderNumber(), event);
            log.info("Order cancellation notification sent successfully to Kafka topic: {}", ORDER_CANCELLED_TOPIC);
            
        } catch (Exception e) {
            log.error("Failed to send order cancellation notification for order: {}", order.getOrderNumber(), e);
        }
    }
    
    /**
     * Send payment confirmation notification
     */
    public void sendPaymentConfirmation(Order order, String transactionId) {
        log.info("Sending payment confirmation notification for order: {}", order.getOrderNumber());
        
        try {
            OrderNotificationEvent event = OrderNotificationEvent.builder()
                    .eventType("PAYMENT_CONFIRMED")
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .userEmail(order.getUserEmail())
                    .userName(order.getUserName())
                    .userPhone(order.getUserPhone())
                    .totalAmount(order.getTotalAmount())
                    .transactionId(transactionId)
                    .message("Your payment has been processed successfully!")
                    .build();
            
            kafkaTemplate.send(PAYMENT_CONFIRMED_TOPIC, order.getOrderNumber(), event);
            log.info("Payment confirmation notification sent successfully to Kafka topic: {}", PAYMENT_CONFIRMED_TOPIC);
            
        } catch (Exception e) {
            log.error("Failed to send payment confirmation notification for order: {}", order.getOrderNumber(), e);
        }
    }
    
    /**
     * Send delivery notification
     */
    public void sendDeliveryNotification(Order order) {
        log.info("Sending delivery notification for order: {}", order.getOrderNumber());
        
        try {
            OrderNotificationEvent event = OrderNotificationEvent.builder()
                    .eventType("ORDER_DELIVERED")
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .userEmail(order.getUserEmail())
                    .userName(order.getUserName())
                    .userPhone(order.getUserPhone())
                    .message("Your order has been delivered successfully!")
                    .deliveryAddress(order.getDeliveryAddress().getFullAddress())
                    .build();
            
            kafkaTemplate.send(ORDER_DELIVERED_TOPIC, order.getOrderNumber(), event);
            log.info("Delivery notification sent successfully to Kafka topic: {}", ORDER_DELIVERED_TOPIC);
            
        } catch (Exception e) {
            log.error("Failed to send delivery notification for order: {}", order.getOrderNumber(), e);
        }
    }
    
    private String getStatusUpdateMessage(Order.OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "Your order has been confirmed and will be processed soon.";
            case PROCESSING -> "Your order is being prepared for shipment.";
            case PACKED -> "Your order has been packed and is ready for pickup.";
            case SHIPPED -> "Your order has been shipped! You can track it using the tracking number.";
            case OUT_FOR_DELIVERY -> "Your order is out for delivery and will reach you soon.";
            case DELIVERED -> "Your order has been delivered successfully! Thank you for shopping with us.";
            case CANCELLED -> "Your order has been cancelled.";
            case RETURNED -> "Your order return has been processed.";
            case REFUNDED -> "Your refund has been processed and will be credited to your account.";
            default -> "Your order status has been updated.";
        };
    }
}

 