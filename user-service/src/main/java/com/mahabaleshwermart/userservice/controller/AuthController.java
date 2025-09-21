package com.mahabaleshwermart.userservice.controller;

import com.mahabaleshwermart.common.dto.ApiResponse;
import com.mahabaleshwermart.userservice.dto.LoginRequest;
import com.mahabaleshwermart.userservice.dto.RegisterRequest;
import com.mahabaleshwermart.userservice.dto.AuthResponse;
import com.mahabaleshwermart.userservice.dto.RefreshTokenRequest;
import com.mahabaleshwermart.userservice.dto.UserDto;
import com.mahabaleshwermart.userservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user authentication, registration, and token management
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * User login endpoint
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return access token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        
        AuthResponse authResponse = authService.login(request);
        
        return ResponseEntity.ok(
            ApiResponse.success(authResponse, "Login successful")
        );
    }
    
    /**
     * User registration endpoint
     */
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        
        AuthResponse authResponse = authService.register(request);
        
        return ResponseEntity.status(201).body(
            ApiResponse.created(authResponse, "Registration successful")
        );
    }
    
    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        
        AuthResponse authResponse = authService.refreshToken(request.getRefreshToken());
        
        return ResponseEntity.ok(
            ApiResponse.success(authResponse, "Token refreshed successfully")
        );
    }
    
    /**
     * User logout endpoint
     */
    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "User logout", description = "Logout user and invalidate tokens")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Logout request received");
        
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        
        return ResponseEntity.ok(
            ApiResponse.success("Logout successful")
        );
    }
    
    /**
     * Get current user profile
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user", description = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
        log.info("Get current user request");
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(
                ApiResponse.unauthorized("Unauthorized: missing or invalid token")
            );
        }

        UserDto user = authService.getCurrentUser(authentication.getName());

        return ResponseEntity.ok(
            ApiResponse.success(user, "User profile retrieved successfully")
        );
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if authentication service is running")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("Authentication service is running")
        );
    }
}