package com.mahabaleshwermart.orderservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for payment gateway settings in order service
 */
@Configuration
@ConfigurationProperties(prefix = "payment")
@Data
public class PaymentConfig {
    
    private boolean enabled = false;
    private boolean mockMode = true;
    private boolean skipPaymentProcessing = true;
    
    /**
     * Check if payment processing should be skipped
     */
    public boolean shouldSkipPaymentProcessing() {
        return !enabled || mockMode || skipPaymentProcessing;
    }
    
    /**
     * Check if we should use mock payment mode
     */
    public boolean shouldUseMockMode() {
        return !enabled || mockMode;
    }
}
