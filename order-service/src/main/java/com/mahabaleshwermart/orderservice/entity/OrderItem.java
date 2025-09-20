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

/**
 * Order Item entity representing individual products within an order
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id"),
    @Index(name = "idx_order_item_product", columnList = "product_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "product_id", nullable = false)
    private String productId; // Reference to product service
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "product_image", nullable = false)
    private String productImage;
    
    @Column(name = "product_sku")
    private String productSku;
    
    @Column(name = "product_category")
    private String productCategory;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;
    
    @Column(name = "product_unit", nullable = false)
    private String productUnit; // kg, pieces, liter, etc.
    
    @Column(nullable = false)
    private int quantity;
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private boolean organic = false;
    
    @Builder.Default
    private boolean fresh = false;
    
    @Column(name = "item_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ItemStatus itemStatus = ItemStatus.ORDERED;
    
    @Column(name = "return_reason", length = 500)
    private String returnReason;
    
    @Column(name = "return_quantity")
    private Integer returnQuantity;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Business logic methods
    @PrePersist
    private void prePersist() {
        calculateTotalPrice();
    }
    
    @PreUpdate
    private void preUpdate() {
        calculateTotalPrice();
    }
    
    private void calculateTotalPrice() {
        if (unitPrice != null && quantity > 0) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity))
                    .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        } else {
            totalPrice = BigDecimal.ZERO;
        }
    }
    
    // Helper methods
    @Transient
    public boolean isOnSale() {
        return originalPrice != null && originalPrice.compareTo(unitPrice) > 0;
    }
    
    @Transient
    public BigDecimal getSavings() {
        if (isOnSale()) {
            BigDecimal savings = originalPrice.subtract(unitPrice);
            return savings.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
    
    @Transient
    public boolean isReturnable() {
        return itemStatus == ItemStatus.DELIVERED && 
               returnQuantity == null;
    }
    
    public enum ItemStatus {
        ORDERED,
        CONFIRMED,
        PACKED,
        SHIPPED,
        DELIVERED,
        RETURNED,
        CANCELLED,
        REFUNDED
    }
} 