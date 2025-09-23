package com.mahabaleshwermart.paymentservice.dto;

import com.mahabaleshwermart.paymentservice.entity.Payment;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for payment initiation requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @Builder.Default
    private String currency = "INR";

    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;

    @NotNull(message = "Gateway provider is required")
    private Payment.GatewayProvider gatewayProvider;

    @NotBlank(message = "Return URL is required")
    @Pattern(regexp = "^https?://.*", message = "Return URL must be a valid HTTP/HTTPS URL")
    private String returnUrl;

    @Pattern(regexp = "^https?://.*", message = "Cancel URL must be a valid HTTP/HTTPS URL")
    private String cancelUrl;

    @Pattern(regexp = "^https?://.*", message = "Callback URL must be a valid HTTP/HTTPS URL")
    private String callbackUrl;

    // Optional customer details for better payment experience
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Optional UPI ID for UPI payments
    private String upiId;

    // Optional description
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    // Optional metadata
    private String metadata;
}
