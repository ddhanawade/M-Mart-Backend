package com.mahabaleshwermart.orderservice.external;

import com.mahabaleshwermart.orderservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for Cart Service communication
 */
@FeignClient(name = "cart-service", url = "http://mahabaleshwer-cart-service:8083")
public interface CartServiceClient {
    
    /**
     * Get user cart with proper header propagation
     */
    @GetMapping("/api/cart")
    ApiResponse<CartSummaryDto> getUserCart(@RequestHeader("X-User-Id") String userId);
    
    /**
     * Validate cart with proper header propagation
     */
    @PostMapping("/api/cart/validate")
    ApiResponse<CartSummaryDto> validateCart(@RequestHeader("X-User-Id") String userId);
    
    /**
     * Validate cart for guest session via header propagation
     */
    @PostMapping("/api/cart/validate")
    ApiResponse<CartSummaryDto> validateCartGuest(@RequestHeader("X-Guest-Session") String guestSessionId,
                                                  @RequestHeader("X-Force-Guest") String forceGuest);
    
    /**
     * Clear user cart with proper header propagation
     */
    @DeleteMapping("/api/cart/clear")
    ApiResponse<Void> clearUserCart(@RequestHeader("X-User-Id") String userId);

    /**
     * Clear guest cart by session header
     */
    @DeleteMapping("/api/cart/clear")
    ApiResponse<Void> clearGuestCart(@RequestHeader("X-Guest-Session") String guestSessionId);
}