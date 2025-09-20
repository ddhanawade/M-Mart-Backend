package com.mahabaleshwermart.cartservice.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product DTO for external service communication
 * Updated to match product-service response structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String category;
    private String subcategory;
    private String image;
    private boolean inStock;
    private int quantity;
    private String unit;
    private BigDecimal rating;
    private int reviewCount;
    private boolean organic;
    private boolean fresh;
    private BigDecimal discount;
    private BigDecimal discountPercentage;
    private boolean featured;
    private String sku;
    private String barcode;
    private LocalDateTime createdAt;
    private NutritionalInfoDto nutritionalInfo;
    private boolean onSale;
    private boolean lowStock;
    
    // Helper methods
    public boolean isAvailable() {
        return inStock && quantity > 0;
    }
    
    /**
     * Nested DTO for nutritional information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionalInfoDto {
        private Integer caloriesPer100g;
        private Double proteinG;
        private Double carbsG;
        private Double fatG;
        private Double fiberG;
        private List<String> vitamins;
    }
} 