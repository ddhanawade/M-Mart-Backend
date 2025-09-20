package com.mahabaleshwermart.cartservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Cart Validation Result DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartValidationResult {

    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
    private List<CartItemDto> unavailableItems;
    private List<CartItemDto> modifiedItems;
    private CartSummaryDto updatedCart;
}
