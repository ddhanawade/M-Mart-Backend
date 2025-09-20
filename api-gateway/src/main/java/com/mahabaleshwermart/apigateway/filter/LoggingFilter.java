package com.mahabaleshwermart.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Logging Filter for API Gateway
 * 
 * This filter logs incoming requests and outgoing responses for monitoring
 * and debugging purposes.
 */
@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            long startTime = System.currentTimeMillis();

            log.info("Incoming request: {} {} from {}",
                request.getMethod(),
                request.getURI(),
                request.getRemoteAddress());

            return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("Outgoing response: {} {} - Status: {} - Duration: {}ms",
                        request.getMethod(),
                        request.getURI(),
                        response.getStatusCode(),
                        duration);
                })
            );
        };
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
