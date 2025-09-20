package com.mahabaleshwermart.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway Application
 * 
 * This service acts as the single entry point for all client requests to the
 * Mahabaleshwer Mart microservices ecosystem. It provides routing, load balancing,
 * security, rate limiting, and monitoring capabilities.
 * 
 * Features:
 * - Request routing to appropriate microservices
 * - Load balancing across service instances
 * - JWT authentication and authorization
 * - Rate limiting and throttling
 * - Circuit breaker pattern for resilience
 * - Request/response logging and monitoring
 * - CORS handling
 * - Request transformation and validation
 * 
 * Note: Eureka client is auto-configured when spring-cloud-starter-netflix-eureka-client
 * dependency is present in the classpath. No explicit @EnableEurekaClient annotation needed.
 * 
 * @author Mahabaleshwer Mart Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
