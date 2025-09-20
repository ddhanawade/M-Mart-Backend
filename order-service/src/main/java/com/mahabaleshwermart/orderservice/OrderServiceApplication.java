package com.mahabaleshwermart.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Order Service Application
 * Handles order processing, payment integration, and order tracking
 */
@SpringBootApplication(scanBasePackages = {
    "com.mahabaleshwermart.orderservice",
    "com.mahabaleshwermart.common"
})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableFeignClients
@EnableTransactionManagement
public class OrderServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
} 