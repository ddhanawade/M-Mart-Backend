package com.mahabaleshwermart.notificationservice.listener;

import com.mahabaleshwermart.notificationservice.service.EmailService;
import com.mahabaleshwermart.notificationservice.service.SmsService;
import com.mahabaleshwermart.common.events.OrderNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Notification Listener
 * Processes order-related notification events from Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {
    
    private final EmailService emailService;
    private final SmsService smsService;
    
    /**
     * Handle order confirmation notifications
     */
    @KafkaListener(topics = "order-confirmed", groupId = "notification-service")
    public void handleOrderConfirmation(@Payload OrderNotificationEvent event, 
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      Acknowledgment acknowledgment) {
        log.info("Processing order confirmation notification for order: {}", event.getOrderNumber());
        
        try {
            // Send email notification
            emailService.sendOrderConfirmationEmail(
                event.getUserEmail(),
                event.getUserName(),
                event.getOrderNumber(),
                "Rs. " + event.getTotalAmount(),
                calculateDeliveryDate()
            );
            
            // Send SMS notification (for critical notifications)
            if (event.getUserPhone() != null && !event.getUserPhone().trim().isEmpty()) {
                smsService.sendOrderConfirmationSms(
                    event.getUserPhone(),
                    event.getUserName(),
                    event.getOrderNumber()
                );
            }
            
            log.info("Order confirmation notifications sent successfully for order: {}", event.getOrderNumber());
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process order confirmation notification for order: {}", event.getOrderNumber(), e);
            // Don't acknowledge on error - message will be retried
        }
    }
    
    /**
     * Handle order status update notifications
     */
    @KafkaListener(topics = "order-status-updated", groupId = "notification-service")
    public void handleOrderStatusUpdate(@Payload OrderNotificationEvent event,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      Acknowledgment acknowledgment) {
        log.info("Processing order status update notification for order: {} - {} to {}", 
                event.getOrderNumber(), event.getOldStatus(), event.getNewStatus());
        
        try {
            // Send email notification
            emailService.sendOrderStatusUpdateEmail(
                event.getUserEmail(),
                event.getUserName(),
                event.getOrderNumber(),
                event.getOldStatus(),
                event.getNewStatus(),
                event.getTrackingNumber()
            );
            
            // Send SMS for critical status updates
            if (event.getUserPhone() != null && isCriticalStatusUpdate(event.getNewStatus())) {
                sendStatusUpdateSms(event);
            }
            
            log.info("Order status update notifications sent successfully for order: {}", event.getOrderNumber());
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process order status update notification for order: {}", event.getOrderNumber(), e);
            // Don't acknowledge on error - message will be retried
        }
    }
    
    /**
     * Handle order cancellation notifications
     */
    @KafkaListener(topics = "order-cancelled", groupId = "notification-service")
    public void handleOrderCancellation(@Payload OrderNotificationEvent event,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      Acknowledgment acknowledgment) {
        log.info("Processing order cancellation notification for order: {}", event.getOrderNumber());
        
        try {
            // Send email notification
            emailService.sendOrderCancellationEmail(
                event.getUserEmail(),
                event.getUserName(),
                event.getOrderNumber(),
                event.getCancellationReason(),
                event.getTotalAmount() != null ? "Rs. " + event.getTotalAmount() : null
            );
            
            // Send SMS notification
            if (event.getUserPhone() != null && !event.getUserPhone().trim().isEmpty()) {
                smsService.sendOrderCancelledSms(
                    event.getUserPhone(),
                    event.getUserName(),
                    event.getOrderNumber()
                );
            }
            
            log.info("Order cancellation notifications sent successfully for order: {}", event.getOrderNumber());
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process order cancellation notification for order: {}", event.getOrderNumber(), e);
            // Don't acknowledge on error - message will be retried
        }
    }
    
    /**
     * Handle payment confirmation notifications
     */
    @KafkaListener(topics = "payment-confirmed", groupId = "notification-service")
    public void handlePaymentConfirmation(@Payload OrderNotificationEvent event,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                        Acknowledgment acknowledgment) {
        log.info("Processing payment confirmation notification for order: {}", event.getOrderNumber());
        
        try {
            // Send email notification
            emailService.sendPaymentConfirmationEmail(
                event.getUserEmail(),
                event.getUserName(),
                event.getOrderNumber(),
                "Rs. " + event.getTotalAmount(),
                event.getTransactionId(),
                "Online Payment" // Could be extracted from event
            );
            
            // Send SMS notification
            if (event.getUserPhone() != null && !event.getUserPhone().trim().isEmpty()) {
                smsService.sendPaymentConfirmationSms(
                    event.getUserPhone(),
                    event.getUserName(),
                    event.getOrderNumber(),
                    event.getTotalAmount().toString()
                );
            }
            
            log.info("Payment confirmation notifications sent successfully for order: {}", event.getOrderNumber());
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process payment confirmation notification for order: {}", event.getOrderNumber(), e);
            // Don't acknowledge on error - message will be retried
        }
    }
    
    /**
     * Handle order delivered notifications
     */
    @KafkaListener(topics = "order-delivered", groupId = "notification-service")
    public void handleOrderDelivered(@Payload OrderNotificationEvent event,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment acknowledgment) {
        log.info("Processing order delivered notification for order: {}", event.getOrderNumber());
        
        try {
            // Send email notification
            emailService.sendDeliveryNotificationEmail(
                event.getUserEmail(),
                event.getUserName(),
                event.getOrderNumber(),
                event.getDeliveryAddress()
            );
            
            // Send SMS notification
            if (event.getUserPhone() != null && !event.getUserPhone().trim().isEmpty()) {
                smsService.sendOrderDeliveredSms(
                    event.getUserPhone(),
                    event.getUserName(),
                    event.getOrderNumber()
                );
            }
            
            log.info("Order delivered notifications sent successfully for order: {}", event.getOrderNumber());
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process order delivered notification for order: {}", event.getOrderNumber(), e);
            // Don't acknowledge on error - message will be retried
        }
    }
    
    private void sendStatusUpdateSms(OrderNotificationEvent event) {
        switch (event.getNewStatus()) {
            case "SHIPPED" -> smsService.sendOrderShippedSms(
                event.getUserPhone(),
                event.getUserName(),
                event.getOrderNumber(),
                event.getTrackingNumber()
            );
            case "OUT_FOR_DELIVERY" -> smsService.sendOutForDeliverySms(
                event.getUserPhone(),
                event.getUserName(),
                event.getOrderNumber()
            );
            case "DELIVERED" -> smsService.sendOrderDeliveredSms(
                event.getUserPhone(),
                event.getUserName(),
                event.getOrderNumber()
            );
        }
    }
    
    private boolean isCriticalStatusUpdate(String status) {
        return "SHIPPED".equals(status) || 
               "OUT_FOR_DELIVERY".equals(status) || 
               "DELIVERED".equals(status) ||
               "CANCELLED".equals(status);
    }
    
    private String calculateDeliveryDate() {
        // Calculate estimated delivery date (3 days from now)
        LocalDateTime deliveryDate = LocalDateTime.now().plusDays(3);
        return deliveryDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }
} 