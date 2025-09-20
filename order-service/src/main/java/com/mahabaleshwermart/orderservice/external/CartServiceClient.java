package com.mahabaleshwermart.orderservice.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for Cart Service communication
 */
@FeignClient(name = "cart-service", path = "/api/cart")
public interface CartServiceClient {
    
    /**
     * Get user cart
     */
    @GetMapping
    CartSummaryDto getUserCart(@RequestParam("userId") String userId);
    
    /**
     * Validate cart
     */
    @PostMapping("/validate")
    CartSummaryDto validateCart(@RequestParam("userId") String userId);
    
    /**
     * Clear user cart
     */
    @DeleteMapping("/clear")
    void clearUserCart(@RequestParam("userId") String userId);
}