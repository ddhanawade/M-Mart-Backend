package com.mahabaleshwermart.orderservice.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for Cart Service communication
 */
@FeignClient(name = "cart-service")
public interface CartServiceClient {
    
    /**
     * Get user cart
     */
    @GetMapping("/api/cart")
    CartSummaryDto getUserCart(@RequestParam("userId") String userId);
    
    /**
     * Validate cart
     */
    @PostMapping("/api/cart/validate")
    CartSummaryDto validateCart(@RequestParam("userId") String userId);
    
    /**
     * Validate cart for guest session via header propagation
     */
    @PostMapping("/api/cart/validate")
    CartSummaryDto validateCartGuest(@RequestHeader("X-Guest-Session") String guestSessionId);
    
    /**
     * Clear user cart
     */
    @DeleteMapping("/api/cart/clear")
    void clearUserCart(@RequestParam("userId") String userId);
}