package com.mahabaleshwermart.cartservice.entity;

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
 * Cart Item entity representing items in a user's shopping cart
 */
@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_user", columnList = "user_id"),
    @Index(name = "idx_cart_session", columnList = "session_id"),
    @Index(name = "idx_cart_product", columnList = "product_id"),
    @Index(name = "idx_cart_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "user_id")
    private String userId; // Reference to user service (null for guest carts)
    
    @Column(name = "session_id")
    private String sessionId; // Session ID for guest carts
    
    @Column(name = "product_id", nullable = false)
    private String productId; // Reference to product service
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "product_image", nullable = false)
    private String productImage;
    
    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;
    
    @Column(name = "product_unit", nullable = false)
    private String productUnit; // kg, pieces, liter, etc.
    
    @Column(nullable = false)
    private int quantity;
    
    @Column(name = "selected_quantity", nullable = false)
    private int selectedQuantity; // Same as quantity for consistency
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Builder.Default
    private boolean active = true;
    
    @Builder.Default
    private boolean available = true; // Product availability
    
    @Column(name = "product_category")
    private String productCategory;
    
    @Column(name = "product_sku")
    private String productSku;
    
    @Builder.Default
    private boolean organic = false;
    
    @Builder.Default
    private boolean fresh = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "added_at")
    private LocalDateTime addedAt;
    
    // Business logic methods
    @PrePersist
    private void prePersist() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
        if (selectedQuantity == 0) {
            selectedQuantity = quantity;
        }
        calculateTotalPrice();
    }
    
    @PreUpdate
    private void preUpdate() {
        calculateTotalPrice();
    }
    
    private void calculateTotalPrice() {
        if (productPrice != null && quantity > 0) {
            totalPrice = productPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            totalPrice = BigDecimal.ZERO;
        }
    }
    
    // Helper methods
    @Transient
    public boolean isOnSale() {
        return originalPrice != null && originalPrice.compareTo(productPrice) > 0;
    }
    
    @Transient
    public BigDecimal getSavings() {
        if (isOnSale()) {
            BigDecimal savings = originalPrice.subtract(productPrice);
            return savings.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
    
    @Transient
    public boolean isGuest() {
        return userId == null && sessionId != null;
    }
    
    @Transient
    public boolean isRegistered() {
        return userId != null;
    }
} 