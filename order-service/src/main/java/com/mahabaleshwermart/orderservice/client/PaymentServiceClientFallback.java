package com.mahabaleshwermart.orderservice.client;

import com.mahabaleshwermart.orderservice.dto.payment.PaymentRequest;
import com.mahabaleshwermart.orderservice.dto.payment.PaymentResponse;
import com.mahabaleshwermart.orderservice.dto.payment.PaymentVerificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for PaymentServiceClient
 * Provides graceful degradation when payment service is unavailable
 */
@Slf4j
@Component
public class PaymentServiceClientFallback implements PaymentServiceClient {

    @Override
    public ResponseEntity<PaymentResponse> initiatePayment(PaymentRequest paymentRequest) {
        log.error("Payment service is unavailable. Cannot initiate payment for order: {}", paymentRequest.getOrderId());
        
        PaymentResponse fallbackResponse = PaymentResponse.builder()
            .paymentId(null)
            .orderId(paymentRequest.getOrderId())
            .userId(paymentRequest.getUserId())
            .amount(paymentRequest.getAmount())
            .currency(paymentRequest.getCurrency())
            .status("FAILED")
            .message("Payment service is currently unavailable. Please try again later.")
            .gatewayProvider("NONE")
            .build();
            
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse);
    }

    @Override
    public ResponseEntity<PaymentResponse> verifyPayment(PaymentVerificationRequest verificationRequest) {
        log.error("Payment service is unavailable. Cannot verify payment: {}", verificationRequest.getPaymentId());
        
        PaymentResponse fallbackResponse = PaymentResponse.builder()
            .paymentId(Long.valueOf(verificationRequest.getPaymentId()))
            .status("FAILED")
            .message("Payment service is currently unavailable. Cannot verify payment.")
            .build();
            
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse);
    }

    @Override
    public ResponseEntity<PaymentResponse> getPayment(String paymentId) {
        log.error("Payment service is unavailable. Cannot fetch payment: {}", paymentId);
        
        PaymentResponse fallbackResponse = PaymentResponse.builder()
            .paymentId(null)
            .status("UNKNOWN")
            .message("Payment service is currently unavailable. Cannot fetch payment details.")
            .build();
            
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse);
    }

    @Override
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(String orderId) {
        log.error("Payment service is unavailable. Cannot fetch payment for order: {}", orderId);
        
        PaymentResponse fallbackResponse = PaymentResponse.builder()
            .orderId(Long.valueOf(orderId))
            .status("UNKNOWN")
            .message("Payment service is currently unavailable. Cannot fetch payment details.")
            .build();
            
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse);
    }

    @Override
    public ResponseEntity<PaymentResponse> createRefund(String paymentId, java.math.BigDecimal amount, String reason) {
        log.error("Payment service is unavailable. Cannot create refund for payment: {}", paymentId);
        
        PaymentResponse fallbackResponse = PaymentResponse.builder()
            .paymentId(null)
            .status("REFUND_FAILED")
            .message("Payment service is currently unavailable. Cannot process refund.")
            .build();
            
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse);
    }

    @Override
    public ResponseEntity<String> healthCheck() {
        log.warn("Payment service health check failed - service unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("Payment service is currently unavailable");
    }
}
