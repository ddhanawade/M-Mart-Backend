package com.mahabaleshwermart.orderservice.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO for payment initiation requests from order service to payment service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @NotNull(message = "Payment method is required")
    private String paymentMethod;

    @NotNull(message = "Gateway provider is required")
    private String gatewayProvider;

    private String description;

    private String customerEmail;

    private String customerPhone;

    private String customerName;

    // Callback/redirect URLs expected by payment-service
    private String returnUrl;   // success/return URL
    private String cancelUrl;   // cancel/failure URL
    private String callbackUrl; // optional server callback URL

    // Additional metadata
    private String orderNumber;
    private String notes;
}
