package com.mahabaleshwermart.cartservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

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

    // Optional: userId can be derived from authenticated principal or gateway headers
    private String userId;

    @Default
    private boolean mergeWithExisting = true; // Merge with existing user cart
}
