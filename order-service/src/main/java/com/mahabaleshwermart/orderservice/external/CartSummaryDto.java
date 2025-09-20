package com.mahabaleshwermart.orderservice.external;

import java.math.BigDecimal;
import java.util.List;

public record CartSummaryDto(
    List<CartItemDto> items,
    int totalItems,
    int totalQuantity,
    BigDecimal subtotal,
    BigDecimal totalSavings
) {}


