package com.mahabaleshwermart.cartservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Move Cart Request DTO (for transferring guest cart to user account)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveCartRequest {

    @NotNull(message = "Session ID is required")
    private String sessionId;

    @NotNull(message = "User ID is required")
    private String userId;

    private boolean mergeWithExisting = true; // Merge with existing user cart
}
