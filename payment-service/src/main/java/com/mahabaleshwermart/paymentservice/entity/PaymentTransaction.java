package com.mahabaleshwermart.paymentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PaymentTransaction entity representing individual payment transactions and refunds
 */
@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_transaction_payment", columnList = "payment_id"),
    @Index(name = "idx_transaction_gateway", columnList = "gateway_transaction_id"),
    @Index(name = "idx_transaction_type", columnList = "transaction_type"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    @Transient
    public boolean isSuccessful() {
        return status == TransactionStatus.SUCCESS;
    }

    @Transient
    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }

    @Transient
    public boolean isPending() {
        return status == TransactionStatus.PENDING;
    }

    @Transient
    public boolean isRefund() {
        return transactionType == TransactionType.REFUND || 
               transactionType == TransactionType.PARTIAL_REFUND;
    }

    public enum TransactionType {
        PAYMENT,
        REFUND,
        PARTIAL_REFUND,
        CHARGEBACK,
        ADJUSTMENT
    }

    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED,
        CANCELLED
    }
}
