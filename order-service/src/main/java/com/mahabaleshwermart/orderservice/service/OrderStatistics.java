package com.mahabaleshwermart.orderservice.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatistics {
    private long totalOrders;
    private BigDecimal totalSpent;
    private BigDecimal averageOrderValue;
}


