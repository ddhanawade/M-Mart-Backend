package com.mahabaleshwermart.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Filter DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProductFilterDto {

    private List<String> categories;
    private List<String> subcategories;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<BigDecimal> ratings;
    private List<String> brands;
    private boolean availableOnly;
    private boolean organicOnly;
    private boolean freshOnly;
}
