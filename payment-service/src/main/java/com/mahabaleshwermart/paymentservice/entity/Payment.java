package com.mahabaleshwermart.paymentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment entity representing payment transactions in the system
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_order", columnList = "order_id"),
    @Index(name = "idx_payment_user", columnList = "user_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_gateway", columnList = "gateway_provider"),
    @Index(name = "idx_payment_created", columnList = "created_at"),
    @Index(name = "idx_payment_gateway_id", columnList = "gateway_payment_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway_provider", nullable = false)
    private GatewayProvider gatewayProvider;

    @Column(name = "gateway_payment_id")
    private String gatewayPaymentId;

    @Column(name = "gateway_order_id")
    private String gatewayOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_url", length = 1000)
    private String paymentUrl;

    @Column(name = "callback_url", length = 500)
    private String callbackUrl;

    @Column(name = "return_url", length = 500)
    private String returnUrl;

    @Column(name = "cancel_url", length = 500)
    private String cancelUrl;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "payment_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal paymentFee = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 10, scale = 2)
    private BigDecimal netAmount;

    // Card details (masked for security)
    @Column(name = "card_last_four")
    private String cardLastFour;

    @Column(name = "card_brand")
    private String cardBrand;

    @Column(name = "card_type")
    private String cardType;

    // UPI details
    @Column(name = "upi_id")
    private String upiId;

    // Bank details
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number_masked")
    private String accountNumberMasked;

    // Wallet details
    @Column(name = "wallet_name")
    private String walletName;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentTransaction> transactions;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business logic methods
    @PrePersist
    private void prePersist() {
        if (netAmount == null && amount != null && paymentFee != null) {
            netAmount = amount.subtract(paymentFee);
        }
    }

    @PreUpdate
    private void preUpdate() {
        if (netAmount == null && amount != null && paymentFee != null) {
            netAmount = amount.subtract(paymentFee);
        }
    }

    // Helper methods
    @Transient
    public boolean isCompleted() {
        return status == PaymentStatus.SUCCESS && paymentDate != null;
    }

    @Transient
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    @Transient
    public boolean isPending() {
        return status == PaymentStatus.PENDING || status == PaymentStatus.PROCESSING;
    }

    @Transient
    public boolean isRefundable() {
        return status == PaymentStatus.SUCCESS && paymentDate != null;
    }

    @Transient
    public String getMaskedPaymentInfo() {
        return switch (paymentMethod) {
            case CREDIT_CARD, DEBIT_CARD -> cardBrand + " ending in " + cardLastFour;
            case UPI -> "UPI: " + (upiId != null ? upiId : "N/A");
            case NET_BANKING -> "Net Banking: " + (bankName != null ? bankName : "N/A");
            case WALLET -> "Wallet: " + (walletName != null ? walletName : gatewayProvider.name());
        };
    }

    public enum PaymentMethod {
        CREDIT_CARD,
        DEBIT_CARD,
        UPI,
        NET_BANKING,
        WALLET
    }

    public enum GatewayProvider {
        RAZORPAY,
        STRIPE,
        PAYPAL
    }

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED,
        CANCELLED,
        REFUNDED,
        PARTIALLY_REFUNDED
    }
}
