package com.mahabaleshwermart.orderservice.external;

import com.mahabaleshwermart.orderservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for User Service communication
 */
@FeignClient(name = "user-service", url = "http://mahabaleshwer-user-service:8081")
public interface UserServiceClient {
    
    /**
     * Get user by ID using internal endpoint (no authentication required)
     */
    @GetMapping("/api/internal/users/{id}")
    ApiResponse<UserDto> getUserById(@PathVariable("id") String id);
}