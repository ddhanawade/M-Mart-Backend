package com.mahabaleshwermart.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Notification Service Application
 * Handles email, SMS, and push notifications for the e-commerce platform
 */
@SpringBootApplication(
    scanBasePackages = {
        "com.mahabaleshwermart.notificationservice",
        "com.mahabaleshwermart.common"
    },
    exclude = {RabbitAutoConfiguration.class}
)
@EnableDiscoveryClient
@EnableCaching
@EnableAsync
public class NotificationServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
} 