package com.mahabaleshwermart.cartservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Cart Service Application
 * Handles shopping cart management with session and database persistence
 */
@SpringBootApplication(scanBasePackages = {
    "com.mahabaleshwermart.cartservice",
    "com.mahabaleshwermart.common"
})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableFeignClients
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600) // 1 hour session timeout
public class CartServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
} 