package com.mahabaleshwermart.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderPaymentRequest {

    @NotNull(message = "Payment method is required")
    private String paymentMethod;

    private String paymentGateway;
    private String cardToken;
    private String upiId;
    private String bankCode;
}


