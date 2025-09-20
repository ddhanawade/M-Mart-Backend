package com.mahabaleshwermart.orderservice.service;

import com.mahabaleshwermart.orderservice.entity.Order;
import com.mahabaleshwermart.orderservice.dto.CreateOrderPaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payment Service
 * Handles payment processing with various payment gateways
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    /**
     * Process payment for an order
     */
    public PaymentResult processPayment(Order order, CreateOrderPaymentRequest paymentRequest) {
        log.info("Processing payment for order: {} using method: {}", 
                order.getOrderNumber(), paymentRequest.getPaymentMethod());
        
        try {
            // Mock payment processing based on payment method
            return switch (paymentRequest.getPaymentMethod().toUpperCase()) {
                case "CREDIT_CARD", "DEBIT_CARD" -> processCardPayment(order, paymentRequest);
                case "UPI" -> processUpiPayment(order, paymentRequest);
                case "NET_BANKING" -> processNetBankingPayment(order, paymentRequest);
                case "WALLET" -> processWalletPayment(order, paymentRequest);
                case "CASH_ON_DELIVERY" -> processCashOnDelivery(order);
                case "BANK_TRANSFER" -> processBankTransfer(order, paymentRequest);
                default -> PaymentResult.failure("Unsupported payment method: " + paymentRequest.getPaymentMethod());
            };
        } catch (Exception e) {
            log.error("Payment processing failed for order: {}", order.getOrderNumber(), e);
            return PaymentResult.failure("Payment processing error: " + e.getMessage());
        }
    }
    
    /**
     * Process refund for an order
     */
    public RefundResult processRefund(Order order, BigDecimal refundAmount, String reason) {
        log.info("Processing refund for order: {} amount: {} reason: {}", 
                order.getOrderNumber(), refundAmount, reason);
        
        try {
            // Mock refund processing
            if (order.getPayment().getPaymentMethod().toString().equals("CASH_ON_DELIVERY")) {
                return RefundResult.failure("Cannot refund COD orders");
            }
            
            // Simulate refund processing delay
            Thread.sleep(1000);
            
            String refundId = "REFUND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            log.info("Refund processed successfully. Refund ID: {}", refundId);
            return RefundResult.success(refundId);
            
        } catch (Exception e) {
            log.error("Refund processing failed for order: {}", order.getOrderNumber(), e);
            return RefundResult.failure("Refund processing error: " + e.getMessage());
        }
    }
    
    // Private payment method handlers
    
    private PaymentResult processCardPayment(Order order, CreateOrderPaymentRequest request) {
        log.info("Processing card payment for order: {}", order.getOrderNumber());
        
        // Mock card payment processing
        if (request.getCardToken() == null || request.getCardToken().trim().isEmpty()) {
            return PaymentResult.failure("Card token is required for card payments");
        }
        
        // Simulate payment gateway call
        try {
            Thread.sleep(2000); // Simulate processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Mock successful payment (90% success rate)
        if (Math.random() < 0.9) {
            String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String transactionId = "TXN-" + System.currentTimeMillis();
            return PaymentResult.success(paymentId, transactionId);
        } else {
            return PaymentResult.failure("Card payment declined by bank");
        }
    }
    
    private PaymentResult processUpiPayment(Order order, CreateOrderPaymentRequest request) {
        log.info("Processing UPI payment for order: {}", order.getOrderNumber());
        
        if (request.getUpiId() == null || request.getUpiId().trim().isEmpty()) {
            return PaymentResult.failure("UPI ID is required for UPI payments");
        }
        
        // Mock UPI payment processing
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Mock successful payment (95% success rate)
        if (Math.random() < 0.95) {
            String paymentId = "UPI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String transactionId = "UPI-" + System.currentTimeMillis();
            return PaymentResult.success(paymentId, transactionId);
        } else {
            return PaymentResult.failure("UPI payment failed - insufficient funds");
        }
    }
    
    private PaymentResult processNetBankingPayment(Order order, CreateOrderPaymentRequest request) {
        log.info("Processing net banking payment for order: {}", order.getOrderNumber());
        
        // Mock net banking processing
        try {
            Thread.sleep(3000); // Net banking takes longer
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String paymentId = "NB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String transactionId = "NB-" + System.currentTimeMillis();
        return PaymentResult.success(paymentId, transactionId);
    }
    
    private PaymentResult processWalletPayment(Order order, CreateOrderPaymentRequest request) {
        log.info("Processing wallet payment for order: {}", order.getOrderNumber());
        
        // Mock wallet payment processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String paymentId = "WALLET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String transactionId = "WALLET-" + System.currentTimeMillis();
        return PaymentResult.success(paymentId, transactionId);
    }
    
    private PaymentResult processCashOnDelivery(Order order) {
        log.info("Processing COD for order: {}", order.getOrderNumber());
        
        // COD is always successful at order time
        String paymentId = "COD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResult.success(paymentId, "COD-PENDING");
    }
    
    private PaymentResult processBankTransfer(Order order, CreateOrderPaymentRequest request) {
        log.info("Processing bank transfer for order: {}", order.getOrderNumber());
        
        // Mock bank transfer processing
        String paymentId = "BT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String transactionId = "BT-" + System.currentTimeMillis();
        return PaymentResult.success(paymentId, transactionId);
    }
}

/**
 * Payment processing result
 */
record PaymentResult(
    boolean successful,
    String paymentId,
    String transactionId,
    String failureReason
) {
    public static PaymentResult success(String paymentId, String transactionId) {
        return new PaymentResult(true, paymentId, transactionId, null);
    }
    
    public static PaymentResult failure(String reason) {
        return new PaymentResult(false, null, null, reason);
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
}

/**
 * Refund processing result
 */
record RefundResult(
    boolean successful,
    String refundId,
    String failureReason
) {
    public static RefundResult success(String refundId) {
        return new RefundResult(true, refundId, null);
    }
    
    public static RefundResult failure(String reason) {
        return new RefundResult(false, null, reason);
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public String getRefundId() {
        return refundId;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
} 