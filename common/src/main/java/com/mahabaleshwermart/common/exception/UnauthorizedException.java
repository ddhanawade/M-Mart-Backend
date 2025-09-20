package com.mahabaleshwermart.common.exception;

/**
 * Exception thrown when user is not authorized to access a resource
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
} 