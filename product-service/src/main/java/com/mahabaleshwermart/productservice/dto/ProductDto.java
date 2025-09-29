package com.mahabaleshwermart.productservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String category;
    private String subcategory;
    private String image;
    private List<String> images;
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
    private BigDecimal weightKg;
    private Integer shelfLifeDays;
    private String storageInstructions;
    private String originCountry;
    private String supplierName;
    private String brand;
    private String farmerName;
    private String season;
    private LocalDateTime createdAt;
    private NutritionalInfoDto nutritionalInfo;
    private List<ProductReviewDto> reviews;
    
    // Helper methods
    public boolean isOnSale() {
        return originalPrice != null && originalPrice.compareTo(price) > 0;
    }
    
    public boolean isLowStock() {
        return quantity > 0 && quantity <= 10;
    }
    
    /**
     * Nutritional Information DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionalInfoDto implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private Integer caloriesPer100g;
        private BigDecimal proteinG;
        private BigDecimal carbsG;
        private BigDecimal fatG;
        private BigDecimal fiberG;
        private List<String> vitamins;
    }
}

