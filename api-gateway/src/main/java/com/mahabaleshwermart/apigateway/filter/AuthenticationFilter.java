package com.mahabaleshwermart.apigateway.filter;

import com.mahabaleshwermart.apigateway.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Authentication Filter for API Gateway
 * 
 * This filter validates JWT tokens for protected routes and adds user information
 * to request headers for downstream services.
 */
@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtService jwtService;

    // Route prefixes that don't require authentication (discovery-based paths)
    private static final List<String> OPEN_API_PREFIXES = List.of(
        // User service public auth APIs
        "/user-service/api/auth",
        // Product catalog is public
        "/product-service/api/products",
        // Cart guest/user endpoints should be accessible without JWT (session/guest handled downstream)
        "/cart-service/api/cart",
        // Actuator and docs for all services
        "/user-service/actuator",
        "/product-service/actuator",
        "/cart-service/actuator",
        "/order-service/actuator",
        "/notification-service/actuator",
        "/api-gateway/actuator",
        "/v3/api-docs",
        "/swagger-ui",
        "/swagger-ui.html",
        "/actuator"
    );

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.debug("Processing request for path: {}", path);

            // Always allow CORS preflight
            if (request.getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            // For open endpoints: if Authorization present and valid, decorate with user headers; never block
            if (isOpenEndpoint(path)) {
                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    try {
                        if (jwtService.validateToken(token)) {
                            String userId = jwtService.extractUserId(token);
                            String username = jwtService.extractUsername(token);
                            String roles = jwtService.extractRoles(token);
                            ServerHttpRequest decorated = request.mutate()
                                .header("X-User-Id", userId)
                                .header("X-Username", username)
                                .header("X-User-Roles", roles)
                                .build();
                            return chain.filter(exchange.mutate().request(decorated).build());
                        }
                    } catch (Exception e) {
                        log.debug("Open endpoint auth decoration skipped due to token error: {}", e.getMessage());
                    }
                }
                return chain.filter(exchange);
            }

            // Check for Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header for protected endpoint: {}", path);
                return onError(exchange, "Authorization header is missing", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format for endpoint: {}", path);
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // Validate JWT token
                if (!jwtService.validateToken(token)) {
                    log.warn("Invalid JWT token for endpoint: {}", path);
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                // Extract user information from token
                String userId = jwtService.extractUserId(token);
                String username = jwtService.extractUsername(token);
                String roles = jwtService.extractRoles(token);

                // Add user information to request headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .header("X-User-Roles", roles)
                    .build();

                ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

                log.debug("Authentication successful for user: {} on path: {}", username, path);
                return chain.filter(modifiedExchange);

            } catch (Exception e) {
                log.error("Authentication error for path: {} - {}", path, e.getMessage());
                return onError(exchange, "Authentication failed", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isOpenEndpoint(String path) {
        return OPEN_API_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}", 
            message, status.value(), java.time.Instant.now());
        
        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
