package com.mahabaleshwermart.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback Controller for API Gateway
 * 
 * Provides fallback responses when downstream services are unavailable
 * or circuit breakers are open.
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        log.warn("User service is currently unavailable - returning fallback response");
        return Mono.just(createFallbackResponse("User Service", "Authentication and user management services are temporarily unavailable"));
    }

    @GetMapping("/product-service")
    public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback() {
        log.warn("Product service is currently unavailable - returning fallback response");
        return Mono.just(createFallbackResponse("Product Service", "Product catalog services are temporarily unavailable"));
    }

    @GetMapping("/cart-service")
    public Mono<ResponseEntity<Map<String, Object>>> cartServiceFallback() {
        log.warn("Cart service is currently unavailable - returning fallback response");
        return Mono.just(createFallbackResponse("Cart Service", "Shopping cart services are temporarily unavailable"));
    }

    @GetMapping("/order-service")
    public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback() {
        log.warn("Order service is currently unavailable - returning fallback response");
        return Mono.just(createFallbackResponse("Order Service", "Order processing services are temporarily unavailable"));
    }

    @GetMapping("/notification-service")
    public Mono<ResponseEntity<Map<String, Object>>> notificationServiceFallback() {
        log.warn("Notification service is currently unavailable - returning fallback response");
        return Mono.just(createFallbackResponse("Notification Service", "Notification services are temporarily unavailable"));
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("service", serviceName);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("suggestion", "Please try again later or contact support if the issue persists");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
