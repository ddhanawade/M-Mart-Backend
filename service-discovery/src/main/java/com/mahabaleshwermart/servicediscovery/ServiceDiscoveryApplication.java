package com.mahabaleshwermart.servicediscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Service Discovery Application
 * 
 * This service provides service registration and discovery capabilities using Netflix Eureka.
 * All microservices in the Mahabaleshwer Mart ecosystem register with this server
 * and use it to discover other services.
 * 
 * Features:
 * - Service registration and discovery
 * - Health monitoring of registered services
 * - Load balancing support
 * - Failover and resilience
 * - Service metadata management
 * - Dashboard for monitoring registered services
 * 
 * @author Mahabaleshwer Mart Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaServer
public class ServiceDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceDiscoveryApplication.class, args);
    }
}
