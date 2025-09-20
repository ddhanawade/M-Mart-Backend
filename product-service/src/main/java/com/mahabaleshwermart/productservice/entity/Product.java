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
import java.util.List;

/**
 * Product entity representing items in the Mahabaleshwer Mart catalog
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_category", columnList = "category"),
    @Index(name = "idx_product_name", columnList = "name"),
    @Index(name = "idx_product_price", columnList = "price"),
    @Index(name = "idx_product_active", columnList = "active"),
    @Index(name = "idx_product_rating", columnList = "rating")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;
    
    private String subcategory;
    
    @Column(nullable = false)
    private String image;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> images;
    
    @Builder.Default
    private boolean inStock = true;
    
    @Builder.Default
    private int quantity = 0;
    
    @Column(nullable = false)
    private String unit; // kg, pieces, liter, etc.
    
    @Builder.Default
    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Builder.Default
    private int reviewCount = 0;
    
    @Builder.Default
    private boolean organic = false;
    
    @Builder.Default
    private boolean fresh = false;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal discount;
    
    @Builder.Default
    private boolean active = true;
    
    @Builder.Default
    private boolean featured = false;
    
    @Column(name = "sku")
    private String sku; // Stock Keeping Unit
    
    @Column(name = "barcode")
    private String barcode;
    
    @Column(name = "weight_kg", precision = 8, scale = 3)
    private BigDecimal weightKg;
    
    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;
    
    @Column(name = "storage_instructions")
    private String storageInstructions;
    
    @Column(name = "origin_country")
    private String originCountry;
    
    @Column(name = "supplier_name")
    private String supplierName;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductReview> reviews;
    
    @Embedded
    private NutritionalInfo nutritionalInfo;
    
    // Calculated properties
    @Transient
    public BigDecimal getDiscountPercentage() {
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal difference = originalPrice.subtract(price);
            return difference.multiply(BigDecimal.valueOf(100))
                    .divide(originalPrice, 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    @Transient
    public boolean isOnSale() {
        return originalPrice != null && originalPrice.compareTo(price) > 0;
    }
    
    @Transient
    public boolean isLowStock() {
        return quantity > 0 && quantity <= 10;
    }
    
    public enum ProductCategory {
        FRUITS,
        VEGETABLES,
        ORGANIC,
        GROCERIES,
        DAIRY,
        BAKERY,
        BEVERAGES,
        SPICES,
        SNACKS,
        PERSONAL_CARE,
        HOUSEHOLD
    }
    
    /**
     * Embedded nutritional information
     */
    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionalInfo implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        @Column(name = "calories_per_100g")
        private Integer caloriesPer100g;
        
        @Column(name = "protein_g", precision = 5, scale = 2)
        private BigDecimal proteinG;
        
        @Column(name = "carbs_g", precision = 5, scale = 2)
        private BigDecimal carbsG;
        
        @Column(name = "fat_g", precision = 5, scale = 2)
        private BigDecimal fatG;
        
        @Column(name = "fiber_g", precision = 5, scale = 2)
        private BigDecimal fiberG;
        
        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "product_vitamins", joinColumns = @JoinColumn(name = "product_id"))
        @Column(name = "vitamin")
        private List<String> vitamins;
    }
} 