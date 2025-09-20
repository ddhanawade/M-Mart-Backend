package com.mahabaleshwermart.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clear Cart Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClearCartRequest {

    private String sessionId; // For guest users
    private boolean clearAll = true; // Clear all items vs only unavailable items
}
