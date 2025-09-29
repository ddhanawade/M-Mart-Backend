package com.mahabaleshwermart.apigateway.config;

import com.mahabaleshwermart.apigateway.filter.AuthenticationFilter;
import com.mahabaleshwermart.apigateway.filter.LoggingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

/**
 * Gateway Configuration
 * 
 * Configures routing rules, filters, and CORS settings for the API Gateway.
 * Routes requests to appropriate microservices based on path patterns.
 */
@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Autowired
    private LoggingFilter loggingFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // User Service Routes
            .route("user-service", r -> r
                .path("/api/v1/auth/**", "/api/v1/users/**")
                .filters(f -> f
                    .filter(loggingFilter.apply(new LoggingFilter.Config()))
                    .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                    .circuitBreaker(config -> config
                        .setName("user-service-cb")
                        .setFallbackUri("forward:/fallback/user-service"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, false)))
                .uri("lb://user-service"))
            
            // Product Service Routes
            .route("product-service", r -> r
                .path("/api/v1/products/**", "/api/v1/categories/**")
                .filters(f -> f
                    .filter(loggingFilter.apply(new LoggingFilter.Config()))
                    .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                    .circuitBreaker(config -> config
                        .setName("product-service-cb")
                        .setFallbackUri("forward:/fallback/product-service"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, false)))
                .uri("lb://product-service"))
            
            // Cart Service Routes
            .route("cart-service", r -> r
                .path("/api/v1/cart/**")
                .filters(f -> f
                    .filter(loggingFilter.apply(new LoggingFilter.Config()))
                    .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                    .circuitBreaker(config -> config
                        .setName("cart-service-cb")
                        .setFallbackUri("forward:/fallback/cart-service"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, false)))
                .uri("lb://cart-service"))
            
            // Order Service Routes
            .route("order-service", r -> r
                .path("/api/v1/orders/**", "/api/v1/payments/**")
                .filters(f -> f
                    .filter(loggingFilter.apply(new LoggingFilter.Config()))
                    .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                    .circuitBreaker(config -> config
                        .setName("order-service-cb")
                        .setFallbackUri("forward:/fallback/order-service"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, false)))
                .uri("lb://order-service"))
            
            // Notification Service Routes
            .route("notification-service", r -> r
                .path("/api/v1/notifications/**")
                .filters(f -> f
                    .filter(loggingFilter.apply(new LoggingFilter.Config()))
                    .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                    .circuitBreaker(config -> config
                        .setName("notification-service-cb")
                        .setFallbackUri("forward:/fallback/notification-service"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, false)))
                .uri("lb://notification-service"))
            
            // Health Check Routes (No Authentication Required)
            .route("health-checks", r -> r
                .path("/actuator/**")
                .filters(f -> f
                    .filter(loggingFilter.apply(new LoggingFilter.Config())))
                .uri("lb://service-discovery"))
            
            .build();
    }

    // Removed duplicate CORS filter; using application.yml spring.cloud.gateway.globalcors instead
}
