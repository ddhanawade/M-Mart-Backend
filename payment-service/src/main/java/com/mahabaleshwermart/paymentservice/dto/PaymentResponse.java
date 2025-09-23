package com.mahabaleshwermart.paymentservice.dto;

import com.mahabaleshwermart.paymentservice.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for payment response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private Payment.PaymentMethod paymentMethod;
    private Payment.GatewayProvider gatewayProvider;
    private Payment.PaymentStatus status;
    private String gatewayPaymentId;
    private String gatewayOrderId;
    private String paymentUrl;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime paymentDate;
    
    // Masked payment details for security
    private String maskedPaymentInfo;
    
    // Additional response fields
    private String message;
    private boolean success;
}
