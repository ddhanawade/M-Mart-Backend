package com.mahabaleshwermart.orderservice.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for User Service communication
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    /**
     * Get user by ID
     */
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") String id);
}