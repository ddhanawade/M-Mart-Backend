package com.mahabaleshwermart.orderservice.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for payment responses from payment service to order service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String gatewayProvider;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String gatewayTransactionId;
    private String message;
    private String errorCode;
    private String errorMessage;
    
    // Payment URLs for frontend integration
    private String paymentUrl;
    private String qrCodeUrl;
    private String publicKey;
    
    // Payment details (masked for security)
    private String cardLastFour;
    private String cardBrand;
    private String upiId;
    private String bankName;
    private String walletName;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paymentCompletedAt;
    
    // Additional metadata
    private String notes;
    private Object gatewayResponse; // Raw gateway response for debugging
}
