package com.mahabaleshwermart.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server Application
 * 
 * This service provides centralized configuration management for all microservices
 * in the Mahabaleshwer Mart ecosystem. It serves configuration files from a Git repository
 * and integrates with Eureka for service discovery.
 * 
 * Features:
 * - Centralized configuration management
 * - Environment-specific configurations
 * - Real-time configuration updates
 * - Security for sensitive configurations
 * - Integration with service discovery
 * 
 * Note: Eureka client is auto-configured when spring-cloud-starter-netflix-eureka-client
 * dependency is present in the classpath. No explicit @EnableEurekaClient annotation needed.
 * 
 * @author Mahabaleshwer Mart Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
