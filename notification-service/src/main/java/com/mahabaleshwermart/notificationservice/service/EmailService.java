package com.mahabaleshwermart.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

/**
 * Email Service
 * Handles sending emails with template support
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${app.notification.email.from:noreply@mahabaleshwermart.com}")
    private String fromEmail;
    
    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;
    
    /**
     * Send simple text email
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        if (!emailEnabled) {
            log.info("Email notifications disabled, skipping email to: {}", to);
            return;
        }
        
        log.info("Sending simple email to: {} with subject: {}", to, subject);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
            
        } catch (MailException e) {
            log.error("Failed to send simple email to: {}", to, e);
        }
    }
    
    /**
     * Send HTML email with template
     */
    @Async
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        if (!emailEnabled) {
            log.info("Email notifications disabled, skipping template email to: {}", to);
            return;
        }
        
        log.info("Sending template email to: {} with template: {}", to, templateName);
        
        try {
            // Process template with variables
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);
            
            // Create and send email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = isHtml
            
            mailSender.send(mimeMessage);
            log.info("Template email sent successfully to: {} using template: {}", to, templateName);
            
        } catch (MessagingException | MailException e) {
            log.error("Failed to send template email to: {} using template: {}", to, templateName, e);
        }
    }
    
    /**
     * Send order confirmation email
     */
    public void sendOrderConfirmationEmail(String to, String userName, String orderNumber, 
                                         String totalAmount, String deliveryDate) {
        String subject = "Order Confirmation - " + orderNumber;
        
        Map<String, Object> variables = Map.of(
            "userName", userName,
            "orderNumber", orderNumber,
            "totalAmount", totalAmount,
            "deliveryDate", deliveryDate,
            "trackingUrl", "https://mahabaleshwermart.com/track/" + orderNumber
        );
        
        sendTemplateEmail(to, subject, "order-confirmation", variables);
    }
    
    /**
     * Send order status update email
     */
    public void sendOrderStatusUpdateEmail(String to, String userName, String orderNumber, 
                                         String oldStatus, String newStatus, String trackingNumber) {
        String subject = "Order Update - " + orderNumber + " is now " + newStatus;
        
        Map<String, Object> variables = Map.of(
            "userName", userName,
            "orderNumber", orderNumber,
            "oldStatus", oldStatus,
            "newStatus", newStatus,
            "trackingNumber", trackingNumber != null ? trackingNumber : "",
            "trackingUrl", trackingNumber != null ? "https://mahabaleshwermart.com/track/" + trackingNumber : ""
        );
        
        sendTemplateEmail(to, subject, "order-status-update", variables);
    }
    
    /**
     * Send order cancellation email
     */
    public void sendOrderCancellationEmail(String to, String userName, String orderNumber, 
                                         String reason, String refundAmount) {
        String subject = "Order Cancelled - " + orderNumber;
        
        Map<String, Object> variables = Map.of(
            "userName", userName,
            "orderNumber", orderNumber,
            "reason", reason != null ? reason : "No reason provided",
            "refundAmount", refundAmount != null ? refundAmount : "N/A",
            "refundInfo", refundAmount != null ? "Your refund will be processed within 5-7 business days." : ""
        );
        
        sendTemplateEmail(to, subject, "order-cancellation", variables);
    }
    
    /**
     * Send payment confirmation email
     */
    public void sendPaymentConfirmationEmail(String to, String userName, String orderNumber, 
                                           String amount, String transactionId, String paymentMethod) {
        String subject = "Payment Confirmation - " + orderNumber;
        
        Map<String, Object> variables = Map.of(
            "userName", userName,
            "orderNumber", orderNumber,
            "amount", amount,
            "transactionId", transactionId,
            "paymentMethod", paymentMethod,
            "paymentDate", java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
        );
        
        sendTemplateEmail(to, subject, "payment-confirmation", variables);
    }
    
    /**
     * Send delivery notification email
     */
    public void sendDeliveryNotificationEmail(String to, String userName, String orderNumber, 
                                            String deliveryAddress) {
        String subject = "Order Delivered - " + orderNumber;
        
        Map<String, Object> variables = Map.of(
            "userName", userName,
            "orderNumber", orderNumber,
            "deliveryAddress", deliveryAddress,
            "deliveryDate", java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")),
            "feedbackUrl", "https://mahabaleshwermart.com/feedback/" + orderNumber
        );
        
        sendTemplateEmail(to, subject, "order-delivered", variables);
    }
    
    /**
     * Send welcome email for new users
     */
    public void sendWelcomeEmail(String to, String userName) {
        String subject = "Welcome to Mahabaleshwer Mart!";
        
        Map<String, Object> variables = Map.of(
            "userName", userName,
            "shopUrl", "https://mahabaleshwermart.com",
            "supportEmail", fromEmail
        );
        
        sendTemplateEmail(to, subject, "welcome", variables);
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String userName, String resetToken) {
        String subject = "Password Reset Request - Mahabaleshwer Mart";
        
        Map<String, Object> variables = Map.of(
            "userName", userName,
            "resetUrl", "https://mahabaleshwermart.com/reset-password?token=" + resetToken,
            "expiryTime", "1 hour"
        );
        
        sendTemplateEmail(to, subject, "password-reset", variables);
    }
} 