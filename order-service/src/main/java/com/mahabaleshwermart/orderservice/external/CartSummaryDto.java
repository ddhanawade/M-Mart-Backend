package com.mahabaleshwermart.orderservice.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CartSummaryDto(
    List<CartItemDto> items,
    int totalItems,
    int totalQuantity,
    BigDecimal subtotal,
    BigDecimal totalSavings,
    BigDecimal deliveryCharge,
    BigDecimal totalAmount,
    boolean hasOutOfStockItems,
    boolean hasUnavailableItems,
    LocalDateTime lastUpdated,
    boolean empty,
    boolean eligibleForFreeDelivery
) {}


