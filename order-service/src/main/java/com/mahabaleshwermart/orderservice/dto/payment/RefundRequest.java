package com.mahabaleshwermart.orderservice.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO for refund requests from order service to payment service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal amount; // If null, full refund will be processed

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    // Additional metadata
    private String refundType; // FULL, PARTIAL
    private String initiatedBy; // CUSTOMER, ADMIN, SYSTEM
}
