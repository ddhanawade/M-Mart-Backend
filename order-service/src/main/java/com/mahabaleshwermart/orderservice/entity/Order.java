package com.mahabaleshwermart.orderservice.entity;

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
 * Order entity representing customer orders in the Mahabaleshwer Mart system
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "order_status"),
    @Index(name = "idx_order_number", columnList = "order_number"),
    @Index(name = "idx_order_created", columnList = "created_at"),
    @Index(name = "idx_order_payment_status", columnList = "payment_status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;
    
    @Column(name = "user_id", nullable = false)
    private String userId; // Reference to user service
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Column(name = "user_email", nullable = false)
    private String userEmail;
    
    @Column(name = "user_phone")
    private String userPhone;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;
    
    @Embedded
    private OrderAddress deliveryAddress;
    
    @Embedded
    private OrderPayment payment;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderTimeline> timeline;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "delivery_charge", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deliveryCharge = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "total_items", nullable = false)
    private int totalItems;
    
    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;
    
    @Column(name = "special_instructions", length = 500)
    private String specialInstructions;
    
    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;
    
    @Column(name = "actual_delivery")
    private LocalDateTime actualDelivery;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    @Column(name = "refund_amount", precision = 10, scale = 2, insertable = false, updatable = false)
    private BigDecimal refundAmount;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    @Column(name = "invoice_number")
    private String invoiceNumber;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Business logic methods
    @PrePersist
    private void prePersist() {
        if (orderNumber == null) {
            orderNumber = generateOrderNumber();
        }
        if (invoiceNumber == null) {
            invoiceNumber = generateInvoiceNumber();
        }
        if (estimatedDelivery == null) {
            estimatedDelivery = LocalDateTime.now().plusDays(3); // Default 3 days
        }
        calculateTotalAmount();
    }
    
    @PreUpdate
    private void preUpdate() {
        calculateTotalAmount();
    }
    
    private void calculateTotalAmount() {
        if (subtotal != null) {
            totalAmount = subtotal
                    .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                    .add(deliveryCharge != null ? deliveryCharge : BigDecimal.ZERO)
                    .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        }
    }
    
    private String generateOrderNumber() {
        // Format: ORD-YYYYMMDD-HHMMSS-XXXX
        return "ORD-" + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
        ) + "-" + String.format("%04d", (int)(Math.random() * 10000));
    }
    
    private String generateInvoiceNumber() {
        // Format: INV-YYYYMMDD-XXXX
        return "INV-" + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
        ) + "-" + String.format("%04d", (int)(Math.random() * 10000));
    }
    
    // Helper methods
    @Transient
    public boolean isCancellable() {
        return orderStatus == OrderStatus.PENDING || 
               orderStatus == OrderStatus.CONFIRMED ||
               orderStatus == OrderStatus.PROCESSING;
    }
    
    @Transient
    public boolean isRefundable() {
        return paymentStatus == PaymentStatus.COMPLETED && 
               (orderStatus == OrderStatus.CANCELLED || orderStatus == OrderStatus.RETURNED);
    }
    
    @Transient
    public boolean isTrackable() {
        return orderStatus == OrderStatus.SHIPPED || 
               orderStatus == OrderStatus.OUT_FOR_DELIVERY;
    }
    
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        PACKED,
        SHIPPED,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELLED,
        RETURNED,
        REFUNDED
    }
    
    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUNDED,
        PARTIALLY_REFUNDED
    }
} 