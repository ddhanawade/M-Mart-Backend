package com.mahabaleshwermart.orderservice.external;

public record CartItemDto(
    String productId,
    String productName,
    String productImage,
    String productSku,
    String productCategory,
    java.math.BigDecimal productPrice,
    java.math.BigDecimal originalPrice,
    String productUnit,
    int quantity,
    java.math.BigDecimal totalPrice,
    boolean organic,
    boolean fresh
) {}


