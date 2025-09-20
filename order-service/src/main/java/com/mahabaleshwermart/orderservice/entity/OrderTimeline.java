package com.mahabaleshwermart.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Order Timeline entity for tracking order status changes and events
 */
@Entity
@Table(name = "order_timeline", indexes = {
    @Index(name = "idx_timeline_order", columnList = "order_id"),
    @Index(name = "idx_timeline_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderTimeline {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private Order.OrderStatus orderStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private Order.PaymentStatus paymentStatus;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "tracking_details", length = 500)
    private String trackingDetails;
    
    @Column(name = "performed_by")
    private String performedBy; // User ID or system
    
    @Column(name = "performed_by_name")
    private String performedByName;
    
    @Column(name = "is_customer_visible")
    @Builder.Default
    private boolean isCustomerVisible = true;
    
    @Column(name = "is_critical")
    @Builder.Default
    private boolean isCritical = false;
    
    @Column(name = "notification_sent")
    @Builder.Default
    private boolean notificationSent = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Factory methods for common events
    public static OrderTimeline orderPlaced(Order order, String performedBy) {
        return OrderTimeline.builder()
                .order(order)
                .eventType(EventType.ORDER_PLACED)
                .title("Order Placed")
                .description("Your order has been successfully placed")
                .orderStatus(Order.OrderStatus.PENDING)
                .performedBy(performedBy)
                .isCustomerVisible(true)
                .isCritical(true)
                .build();
    }
    
    public static OrderTimeline paymentCompleted(Order order, String transactionId) {
        return OrderTimeline.builder()
                .order(order)
                .eventType(EventType.PAYMENT_COMPLETED)
                .title("Payment Successful")
                .description("Payment has been successfully processed. Transaction ID: " + transactionId)
                .paymentStatus(Order.PaymentStatus.COMPLETED)
                .performedBy("SYSTEM")
                .performedByName("Payment Gateway")
                .isCustomerVisible(true)
                .isCritical(true)
                .build();
    }
    
    public static OrderTimeline orderConfirmed(Order order, String performedBy) {
        return OrderTimeline.builder()
                .order(order)
                .eventType(EventType.ORDER_CONFIRMED)
                .title("Order Confirmed")
                .description("Your order has been confirmed and is being prepared")
                .orderStatus(Order.OrderStatus.CONFIRMED)
                .performedBy(performedBy)
                .performedByName("Store Team")
                .isCustomerVisible(true)
                .isCritical(true)
                .build();
    }
    
    public static OrderTimeline orderShipped(Order order, String trackingNumber, String performedBy) {
        return OrderTimeline.builder()
                .order(order)
                .eventType(EventType.ORDER_SHIPPED)
                .title("Order Shipped")
                .description("Your order has been shipped. Tracking number: " + trackingNumber)
                .orderStatus(Order.OrderStatus.SHIPPED)
                .trackingDetails("Tracking Number: " + trackingNumber)
                .performedBy(performedBy)
                .performedByName("Logistics Team")
                .isCustomerVisible(true)
                .isCritical(true)
                .build();
    }
    
    public static OrderTimeline orderDelivered(Order order, String location, String performedBy) {
        return OrderTimeline.builder()
                .order(order)
                .eventType(EventType.ORDER_DELIVERED)
                .title("Order Delivered")
                .description("Your order has been successfully delivered")
                .orderStatus(Order.OrderStatus.DELIVERED)
                .location(location)
                .performedBy(performedBy)
                .performedByName("Delivery Partner")
                .isCustomerVisible(true)
                .isCritical(true)
                .build();
    }
    
    public static OrderTimeline orderCancelled(Order order, String reason, String performedBy) {
        return OrderTimeline.builder()
                .order(order)
                .eventType(EventType.ORDER_CANCELLED)
                .title("Order Cancelled")
                .description("Order has been cancelled. Reason: " + reason)
                .orderStatus(Order.OrderStatus.CANCELLED)
                .performedBy(performedBy)
                .isCustomerVisible(true)
                .isCritical(true)
                .build();
    }
    
    public enum EventType {
        ORDER_PLACED,
        PAYMENT_INITIATED,
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        ORDER_CONFIRMED,
        ORDER_PROCESSING,
        ORDER_PACKED,
        ORDER_SHIPPED,
        OUT_FOR_DELIVERY,
        DELIVERY_ATTEMPTED,
        ORDER_DELIVERED,
        ORDER_CANCELLED,
        ORDER_RETURNED,
        REFUND_INITIATED,
        REFUND_COMPLETED,
        CUSTOMER_NOTE,
        INTERNAL_NOTE
    }
} 