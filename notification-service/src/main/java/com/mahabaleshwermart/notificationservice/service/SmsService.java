package com.mahabaleshwermart.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * SMS Service
 * Handles sending SMS notifications via external SMS gateway
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${app.notification.sms.enabled:true}")
    private boolean smsEnabled;
    
    @Value("${app.notification.sms.api-key:demo-key}")
    private String smsApiKey;
    
    @Value("${app.notification.sms.sender-id:MHMART}")
    private String senderId;
    
    @Value("${app.notification.sms.gateway-url:https://api.textlocal.in/send/}")
    private String gatewayUrl;
    
    /**
     * Send SMS message
     */
    @Async
    public void sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.info("SMS notifications disabled, skipping SMS to: {}", phoneNumber);
            return;
        }
        
        log.info("Sending SMS to: {} with message: {}", phoneNumber, message);
        
        try {
            // Mock SMS sending - in production, integrate with actual SMS gateway
            simulateSmsGatewayCall(phoneNumber, message);
            log.info("SMS sent successfully to: {}", phoneNumber);
            
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
        }
    }
    
    /**
     * Send order confirmation SMS
     */
    public void sendOrderConfirmationSms(String phoneNumber, String userName, String orderNumber) {
        String message = String.format(
            "Hi %s, your order %s has been confirmed! Track your order at mahabaleshwermart.com/track/%s - Mahabaleshwer Mart",
            userName, orderNumber, orderNumber
        );
        sendSms(phoneNumber, message);
    }
    
    /**
     * Send order shipped SMS
     */
    public void sendOrderShippedSms(String phoneNumber, String userName, String orderNumber, String trackingNumber) {
        String message = String.format(
            "Hi %s, your order %s has been shipped! Tracking: %s. Track at mahabaleshwermart.com/track/%s - Mahabaleshwer Mart",
            userName, orderNumber, trackingNumber, trackingNumber
        );
        sendSms(phoneNumber, message);
    }
    
    /**
     * Send order delivered SMS
     */
    public void sendOrderDeliveredSms(String phoneNumber, String userName, String orderNumber) {
        String message = String.format(
            "Hi %s, your order %s has been delivered! Thank you for shopping with Mahabaleshwer Mart. Rate your experience: mahabaleshwermart.com/feedback/%s",
            userName, orderNumber, orderNumber
        );
        sendSms(phoneNumber, message);
    }
    
    /**
     * Send order cancelled SMS
     */
    public void sendOrderCancelledSms(String phoneNumber, String userName, String orderNumber) {
        String message = String.format(
            "Hi %s, your order %s has been cancelled. If payment was made, refund will be processed within 5-7 days. - Mahabaleshwer Mart",
            userName, orderNumber
        );
        sendSms(phoneNumber, message);
    }
    
    /**
     * Send OTP SMS
     */
    public void sendOtpSms(String phoneNumber, String otp) {
        String message = String.format(
            "Your OTP for Mahabaleshwer Mart is: %s. Valid for 10 minutes. Do not share with anyone. - Mahabaleshwer Mart",
            otp
        );
        sendSms(phoneNumber, message);
    }
    
    /**
     * Send payment confirmation SMS
     */
    public void sendPaymentConfirmationSms(String phoneNumber, String userName, String orderNumber, String amount) {
        String message = String.format(
            "Hi %s, payment of Rs.%s for order %s has been confirmed. Thank you! - Mahabaleshwer Mart",
            userName, amount, orderNumber
        );
        sendSms(phoneNumber, message);
    }
    
    /**
     * Send out for delivery SMS
     */
    public void sendOutForDeliverySms(String phoneNumber, String userName, String orderNumber) {
        String message = String.format(
            "Hi %s, your order %s is out for delivery and will reach you soon! - Mahabaleshwer Mart",
            userName, orderNumber
        );
        sendSms(phoneNumber, message);
    }
    
    private void simulateSmsGatewayCall(String phoneNumber, String message) {
        // Mock implementation - in production, replace with actual SMS gateway integration
        
        try {
            // Simulate API call delay
            Thread.sleep(500);
            
            // Mock API request (example with TextLocal-like API)
            Map<String, String> requestBody = Map.of(
                "apikey", smsApiKey,
                "numbers", phoneNumber,
                "message", message,
                "sender", senderId
            );
            
            // In production, uncomment and configure actual SMS gateway:
            /*
            WebClient webClient = webClientBuilder.build();
            String response = webClient.post()
                    .uri(gatewayUrl)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.debug("SMS Gateway Response: {}", response);
            */
            
            // For demo, just log the SMS content
            log.info("Mock SMS Gateway: Sent SMS to {} - {}", phoneNumber, message);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("SMS sending interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS via gateway", e);
        }
    }
} 