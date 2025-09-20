package com.mahabaleshwermart.orderservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Embeddable OrderPayment entity for payment information
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPayment {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "payment_gateway")
    private String paymentGateway; // Stripe, PayPal, Razorpay, etc.
    
    @Column(name = "payment_id")
    private String paymentId; // External payment gateway ID
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "payment_reference")
    private String paymentReference;
    
    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;
    
    @Column(name = "payment_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal paymentFee = BigDecimal.ZERO;
    
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "INR";
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "payment_response", length = 1000)
    private String paymentResponse; // JSON response from gateway
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "refund_id")
    private String refundId;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(name = "refund_date")
    private LocalDateTime refundDate;
    
    @Column(name = "refund_reason", length = 500)
    private String refundReason;
    
    // Card details (for display purposes only - never store full card details)
    @Column(name = "card_last_four")
    private String cardLastFour;
    
    @Column(name = "card_brand")
    private String cardBrand; // Visa, MasterCard, etc.
    
    // UPI details
    @Column(name = "upi_id")
    private String upiId;
    
    // Bank transfer details
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "account_number_masked")
    private String accountNumberMasked;
    
    // Helper methods
    public boolean isCompleted() {
        return paymentDate != null && paidAmount != null && 
               paidAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isRefunded() {
        return refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public String getMaskedPaymentInfo() {
        return switch (paymentMethod) {
            case CREDIT_CARD, DEBIT_CARD -> cardBrand + " ending in " + cardLastFour;
            case UPI -> "UPI: " + upiId;
            case NET_BANKING -> "Net Banking: " + bankName;
            case WALLET -> "Wallet: " + paymentGateway;
            case CASH_ON_DELIVERY -> "Cash on Delivery";
            case BANK_TRANSFER -> "Bank Transfer: " + accountNumberMasked;
        };
    }
    
    public enum PaymentMethod {
        CREDIT_CARD,
        DEBIT_CARD,
        UPI,
        NET_BANKING,
        WALLET,
        CASH_ON_DELIVERY,
        BANK_TRANSFER
    }
} 