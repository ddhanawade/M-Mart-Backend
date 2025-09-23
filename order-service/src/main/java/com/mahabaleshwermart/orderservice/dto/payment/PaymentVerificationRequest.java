package com.mahabaleshwermart.orderservice.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for payment verification requests from order service to payment service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationRequest {

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotBlank(message = "Gateway order ID is required")
    private String gatewayOrderId;

    @NotBlank(message = "Gateway payment ID is required")
    private String gatewayPaymentId;

    @NotBlank(message = "Payment signature is required")
    private String paymentSignature;

    // Additional verification data
    private String gatewayProvider;
    private String amount;
    private String currency;
}
