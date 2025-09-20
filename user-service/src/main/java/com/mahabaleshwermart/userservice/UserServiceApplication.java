package com.mahabaleshwermart.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * User Service Application
 * Handles authentication, user management, and address management
 */
@SpringBootApplication(scanBasePackages = {
    "com.mahabaleshwermart.userservice",
    "com.mahabaleshwermart.common"
})
@EnableDiscoveryClient
@EnableJpaAuditing
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
} 