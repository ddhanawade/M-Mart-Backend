package com.mahabaleshwermart.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Product Search Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProductSearchRequest {

    private String query;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating;
    private Boolean inStock;
    private Boolean organic;
    private Boolean fresh;
    private Boolean featured;
    private String sortBy; // price, rating, name, created_at
    private String sortDirection; // asc, desc
    private int page = 0;
    private int size = 20;
}
