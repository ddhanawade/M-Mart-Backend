package com.mahabaleshwermart.orderservice.controller;

import com.mahabaleshwermart.orderservice.dto.payment.PaymentResponse;
import com.mahabaleshwermart.orderservice.dto.payment.PaymentVerificationRequest;
import com.mahabaleshwermart.orderservice.dto.payment.RefundRequest;
import com.mahabaleshwermart.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Payment Controller for Order Service
 * Handles payment operations for orders through payment service integration
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Payment", description = "Payment operations for orders")
public class PaymentController {

    private final OrderService orderService;

    /**
     * Initiate payment for an order
     */
    @PostMapping("/{orderId}/payment/initiate")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Initiate payment for an order", 
               description = "Start the payment process for a pending order")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "Payment method") @RequestParam String paymentMethod,
            @Parameter(description = "Gateway provider") @RequestParam(defaultValue = "RAZORPAY") String gatewayProvider) {
        
        log.info("Initiating payment for order: {} with method: {} via gateway: {}", 
            orderId, paymentMethod, gatewayProvider);
            
        PaymentResponse response = orderService.initiatePayment(orderId, paymentMethod, gatewayProvider);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify payment completion
     */
    @PostMapping("/{orderId}/payment/verify")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Verify payment completion", 
               description = "Verify payment signature and update order status")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody PaymentVerificationRequest verificationRequest) {
        
        log.info("Verifying payment for order: {}", orderId);
        
        PaymentResponse response = orderService.verifyPayment(orderId, verificationRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Process refund for an order
     */
    @PostMapping("/{orderId}/payment/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Process refund for an order", 
               description = "Initiate refund process for a completed payment")
    public ResponseEntity<PaymentResponse> processRefund(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody RefundRequest refundRequest) {
        
        log.info("Processing refund for order: {}", orderId);
        
        PaymentResponse response = orderService.processRefund(orderId, refundRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment status for an order
     */
    @GetMapping("/{orderId}/payment/status")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get payment status", 
               description = "Get current payment status for an order")
    public ResponseEntity<PaymentResponse> getPaymentStatus(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        
        log.info("Getting payment status for order: {}", orderId);
        
        // This would call the payment service to get current payment status
        // For now, we'll return a simple response
        PaymentResponse response = PaymentResponse.builder()
            .orderId(orderId)
            .status("PENDING")
            .message("Payment status check - integration pending")
            .build();
            
        return ResponseEntity.ok(response);
    }
}
