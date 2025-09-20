package com.mahabaleshwermart.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard API Response wrapper for all microservices
 * Provides consistent response format across the entire platform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private int statusCode;
    private String path;
    private LocalDateTime timestamp;
    
    // Success response methods
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .statusCode(200)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .statusCode(200)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .statusCode(201)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // Error response methods
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, List<String> errors, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(message, 400);
    }
    
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(message, 401);
    }
    
    public static <T> ApiResponse<T> forbidden(String message) {
        return error(message, 403);
    }
    
    public static <T> ApiResponse<T> notFound(String message) {
        return error(message, 404);
    }
    
    public static <T> ApiResponse<T> internalServerError(String message) {
        return error(message, 500);
    }
    
    // Validation error response
    public static <T> ApiResponse<T> validationError(List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message("Validation failed")
                .errors(errors)
                .statusCode(400)
                .timestamp(LocalDateTime.now())
                .build();
    }
} 