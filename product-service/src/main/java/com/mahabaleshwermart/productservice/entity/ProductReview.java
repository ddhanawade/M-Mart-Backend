package com.mahabaleshwermart.productservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Review entity for customer feedback and ratings
 */
@Entity
@Table(name = "product_reviews", indexes = {
    @Index(name = "idx_review_product", columnList = "product_id"),
    @Index(name = "idx_review_user", columnList = "user_id"),
    @Index(name = "idx_review_rating", columnList = "rating"),
    @Index(name = "idx_review_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProductReview implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "user_id", nullable = false)
    private String userId; // Reference to user service
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating; // 1.0 to 5.0
    
    @Column(length = 1000)
    private String comment;
    
    @Column(length = 200)
    private String title;
    
    @Builder.Default
    private boolean verified = false; // Verified purchase
    
    @Builder.Default
    private boolean helpful = false;
    
    @Builder.Default
    private int helpfulCount = 0;
    
    @Builder.Default
    private boolean active = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 