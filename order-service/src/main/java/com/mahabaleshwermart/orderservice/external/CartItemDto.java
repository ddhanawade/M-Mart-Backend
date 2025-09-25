package com.mahabaleshwermart.orderservice.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CartItemDto(
    String id,
    String userId,
    String productId,
    String productName,
    String productImage,
    BigDecimal productPrice,
    BigDecimal originalPrice,
    String productUnit,
    int quantity,
    int selectedQuantity,
    BigDecimal totalPrice,
    boolean available,
    String productCategory,
    String productSku,
    boolean organic,
    boolean fresh,
    LocalDateTime addedAt,
    LocalDateTime createdAt,
    BigDecimal savings,
    boolean onSale
) {}


