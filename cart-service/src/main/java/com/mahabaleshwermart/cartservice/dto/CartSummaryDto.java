package com.mahabaleshwermart.cartservice.dto;

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
 * Cart Summary DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartSummaryDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<CartItemDto> items;
    private int totalItems;
    private int totalQuantity;
    private BigDecimal subtotal;
    private BigDecimal totalSavings;
    private BigDecimal deliveryCharge;
    private BigDecimal totalAmount;
    private boolean hasOutOfStockItems;
    private boolean hasUnavailableItems;
    private LocalDateTime lastUpdated;

    // Helper methods
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public boolean isEligibleForFreeDelivery() {
        return subtotal != null && subtotal.compareTo(BigDecimal.valueOf(500)) >= 0;
    }
}
