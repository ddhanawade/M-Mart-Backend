package com.mahabaleshwermart.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Product Service Application
 * Handles product catalog, categories, search, and inventory management
 */
@SpringBootApplication(scanBasePackages = {
    "com.mahabaleshwermart.productservice",
    "com.mahabaleshwermart.common"
})
@ComponentScan(basePackages = {
    "com.mahabaleshwermart.productservice.controller",
    "com.mahabaleshwermart.productservice.service",
    "com.mahabaleshwermart.productservice.config",
    "com.mahabaleshwermart.productservice.mapper",
    "com.mahabaleshwermart.common"
})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class ProductServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
} 