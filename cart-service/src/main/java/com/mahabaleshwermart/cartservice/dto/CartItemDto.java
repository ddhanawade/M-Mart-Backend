package com.mahabaleshwermart.cartservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cart Item Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String userId;
    private String sessionId;
    private String productId;
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
    private BigDecimal originalPrice;
    private String productUnit;
    private int quantity;
    private int selectedQuantity;
    private BigDecimal totalPrice;
    private boolean available;
    private String productCategory;
    private String productSku;
    private boolean organic;
    private boolean fresh;
    private LocalDateTime addedAt;
    private LocalDateTime createdAt;
    
    // Helper methods
    public boolean isOnSale() {
        return originalPrice != null && originalPrice.compareTo(productPrice) > 0;
    }
    
    public BigDecimal getSavings() {
        if (isOnSale()) {
            BigDecimal savings = originalPrice.subtract(productPrice);
            return savings.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}











