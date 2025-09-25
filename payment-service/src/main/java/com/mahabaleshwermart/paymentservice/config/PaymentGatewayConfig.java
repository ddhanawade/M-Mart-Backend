package com.mahabaleshwermart.paymentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for payment gateway settings
 */
@Configuration
@ConfigurationProperties(prefix = "payment")
@Data
public class PaymentGatewayConfig {
    
    private boolean enabled = false;
    private boolean mockMode = true;
    
    private RazorpayConfig razorpay = new RazorpayConfig();
    private StripeConfig stripe = new StripeConfig();
    private PaypalConfig paypal = new PaypalConfig();
    
    @Data
    public static class RazorpayConfig {
        private boolean enabled = false;
        private String keyId;
        private String keySecret;
        private String webhookSecret;
        private String baseUrl;
        private int timeout = 30000;
    }
    
    @Data
    public static class StripeConfig {
        private boolean enabled = false;
        private String publicKey;
        private String secretKey;
        private String webhookSecret;
        private String baseUrl;
    }
    
    @Data
    public static class PaypalConfig {
        private boolean enabled = false;
        private String clientId;
        private String clientSecret;
        private String mode;
        private String baseUrl;
    }
    
    /**
     * Check if any payment gateway is enabled
     */
    public boolean isAnyGatewayEnabled() {
        return enabled && (razorpay.enabled || stripe.enabled || paypal.enabled);
    }
    
    /**
     * Check if we should use mock mode
     */
    public boolean shouldUseMockMode() {
        return !enabled || mockMode || !isAnyGatewayEnabled();
    }
}
