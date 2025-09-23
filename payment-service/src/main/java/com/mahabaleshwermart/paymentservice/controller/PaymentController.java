package com.mahabaleshwermart.paymentservice.controller;

import com.mahabaleshwermart.paymentservice.dto.PaymentRequest;
import com.mahabaleshwermart.paymentservice.dto.PaymentResponse;
import com.mahabaleshwermart.paymentservice.dto.PaymentVerificationRequest;
import com.mahabaleshwermart.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for payment operations
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Controller", description = "APIs for payment management")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Initiate a new payment", description = "Creates a new payment and returns payment URL for gateway")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment initiated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payment request"),
        @ApiResponse(responseCode = "409", description = "Payment already exists for order"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/initiate")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest paymentRequest) {
        
        log.info("Received payment initiation request for order: {}", paymentRequest.getOrderId());
        
        try {
            PaymentResponse response = paymentService.initiatePayment(paymentRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            log.warn("Payment already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(PaymentResponse.builder()
                            .message(e.getMessage())
                            .success(false)
                            .build());
        } catch (Exception e) {
            log.error("Error initiating payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.builder()
                            .message("Failed to initiate payment")
                            .success(false)
                            .build());
        }
    }

    @Operation(summary = "Verify payment", description = "Verifies payment signature and updates payment status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid verification request"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/verify")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest verificationRequest) {
        
        log.info("Received payment verification request for payment: {}", verificationRequest.getPaymentId());
        
        try {
            PaymentResponse response = paymentService.verifyPayment(verificationRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Payment not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(PaymentResponse.builder()
                            .message(e.getMessage())
                            .success(false)
                            .build());
        } catch (Exception e) {
            log.error("Error verifying payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.builder()
                            .message("Failed to verify payment")
                            .success(false)
                            .build());
        }
    }

    @Operation(summary = "Get payment by ID", description = "Retrieves payment details by payment ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPayment(
            @Parameter(description = "Payment ID") @PathVariable String paymentId) {
        
        log.info("Received request to get payment: {}", paymentId);
        
        try {
            PaymentResponse response = paymentService.getPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Payment not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(PaymentResponse.builder()
                            .message(e.getMessage())
                            .success(false)
                            .build());
        } catch (Exception e) {
            log.error("Error retrieving payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.builder()
                            .message("Failed to retrieve payment")
                            .success(false)
                            .build());
        }
    }

    @Operation(summary = "Get payment by order ID", description = "Retrieves payment details by order ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        
        log.info("Received request to get payment for order: {}", orderId);
        
        try {
            PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Payment not found for order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(PaymentResponse.builder()
                            .message(e.getMessage())
                            .success(false)
                            .build());
        } catch (Exception e) {
            log.error("Error retrieving payment for order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.builder()
                            .message("Failed to retrieve payment")
                            .success(false)
                            .build());
        }
    }

    @Operation(summary = "Get payments by user ID", description = "Retrieves all payments for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        log.info("Received request to get payments for user: {}", userId);
        
        try {
            List<PaymentResponse> responses = paymentService.getPaymentsByUserId(userId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error retrieving payments for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @Operation(summary = "Create refund", description = "Creates a refund for a successful payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refund created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid refund request"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> createRefund(
            @Parameter(description = "Payment ID") @PathVariable String paymentId,
            @Parameter(description = "Refund amount") @RequestParam BigDecimal amount,
            @Parameter(description = "Refund reason") @RequestParam(required = false) String reason) {
        
        log.info("Received refund request for payment: {} with amount: {}", paymentId, amount);
        
        try {
            PaymentResponse response = paymentService.createRefund(paymentId, amount, reason);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid refund request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PaymentResponse.builder()
                            .message(e.getMessage())
                            .success(false)
                            .build());
        } catch (IllegalStateException e) {
            log.warn("Invalid payment state for refund: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PaymentResponse.builder()
                            .message(e.getMessage())
                            .success(false)
                            .build());
        } catch (Exception e) {
            log.error("Error creating refund: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.builder()
                            .message("Failed to create refund")
                            .success(false)
                            .build());
        }
    }

    @Operation(summary = "Health check", description = "Health check endpoint for payment service")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment Service is healthy");
    }
}
