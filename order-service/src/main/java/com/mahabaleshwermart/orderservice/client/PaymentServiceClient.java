package com.mahabaleshwermart.orderservice.client;

import com.mahabaleshwermart.orderservice.dto.payment.PaymentRequest;
import com.mahabaleshwermart.orderservice.dto.payment.PaymentResponse;
import com.mahabaleshwermart.orderservice.dto.payment.PaymentVerificationRequest;
import com.mahabaleshwermart.orderservice.dto.payment.RefundRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for Payment Service integration
 * Handles communication between Order Service and Payment Service
 */
@FeignClient(
    name = "payment-service",
    url = "${payment-service.url:http://localhost:8086}",
    fallback = PaymentServiceClientFallback.class
)
public interface PaymentServiceClient {

    /**
     * Initiate a payment for an order
     */
    @PostMapping("/api/payments/initiate")
    ResponseEntity<PaymentResponse> initiatePayment(@RequestBody PaymentRequest paymentRequest);

    /**
     * Verify payment after completion
     */
    @PostMapping("/api/payments/verify")
    ResponseEntity<PaymentResponse> verifyPayment(@RequestBody PaymentVerificationRequest verificationRequest);

    /**
     * Get payment details by payment ID
     */
    @GetMapping("/api/payments/{paymentId}")
    ResponseEntity<PaymentResponse> getPayment(@PathVariable("paymentId") Long paymentId);

    /**
     * Get payment details by order ID
     */
    @GetMapping("/api/payments/order/{orderId}")
    ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable("orderId") Long orderId);

    /**
     * Create a refund for a payment
     */
    @PostMapping("/api/payments/{paymentId}/refund")
    ResponseEntity<PaymentResponse> createRefund(
        @PathVariable("paymentId") Long paymentId,
        @RequestBody RefundRequest refundRequest
    );

    /**
     * Health check endpoint
     */
    @GetMapping("/api/payments/health")
    ResponseEntity<String> healthCheck();
}
