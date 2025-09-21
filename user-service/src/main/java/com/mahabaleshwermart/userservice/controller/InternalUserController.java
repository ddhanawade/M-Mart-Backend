package com.mahabaleshwermart.userservice.controller;

import com.mahabaleshwermart.common.dto.ApiResponse;
import com.mahabaleshwermart.userservice.dto.UserDto;
import com.mahabaleshwermart.userservice.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal User Controller for service-to-service communication
 * These endpoints bypass JWT authentication for internal microservice calls
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
@Tag(name = "Internal User Management", description = "Internal endpoints for service-to-service communication")
public class InternalUserController {

    private final UserProfileService userProfileService;

    /**
     * Get user by ID for internal service calls
     * This endpoint bypasses JWT authentication for service-to-service communication
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID (Internal)", description = "Get user profile by ID for internal service calls")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable String userId) {
        log.info("Internal service request: Get user by ID: {}", userId);
        
        try {
            UserDto user = userProfileService.getUserById(userId);
            return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
        } catch (Exception e) {
            log.warn("Failed to retrieve user by ID: {}. Error: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Health check for internal user service
     */
    @GetMapping("/health")
    @Operation(summary = "Internal User Service Health", description = "Health check for internal user service")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Internal User Service is running", "Service is healthy"));
    }
}
