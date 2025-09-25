package com.mahabaleshwermart.paymentservice.controller;

import com.mahabaleshwermart.paymentservice.config.PaymentGatewayConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mock payment controller for testing payment flow without actual gateway integration
 */
@RestController
@RequestMapping("/api/payments/mock")
@Tag(name = "Mock Payment", description = "Mock payment endpoints for testing")
@Slf4j
@RequiredArgsConstructor
public class MockPaymentController {

    private final PaymentGatewayConfig paymentGatewayConfig;

    /**
     * Mock payment checkout page
     */
    @GetMapping("/checkout/{orderId}")
    @Operation(summary = "Mock payment checkout page", description = "Simulates payment gateway checkout page for testing")
    public ResponseEntity<String> mockCheckout(@PathVariable String orderId) {
        log.info("Mock checkout accessed for order: {}", orderId);
        
        if (!paymentGatewayConfig.shouldUseMockMode()) {
            return ResponseEntity.badRequest().body("Mock mode is disabled");
        }
        
        String mockCheckoutPage = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Mock Payment Gateway - Test Mode</title>
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 50px auto; padding: 20px; }
                    .container { border: 1px solid #ddd; border-radius: 8px; padding: 30px; background-color: #f9f9f9; }
                    .header { text-align: center; color: #333; margin-bottom: 30px; }
                    .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 4px; margin-bottom: 20px; }
                    .form-group { margin-bottom: 20px; }
                    .btn { background-color: #28a745; color: white; padding: 12px 24px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }
                    .btn:hover { background-color: #218838; }
                    .btn-cancel { background-color: #dc3545; }
                    .btn-cancel:hover { background-color: #c82333; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>🧪 Mock Payment Gateway</h2>
                        <p>Test Mode - No Real Payment Processing</p>
                    </div>
                    
                    <div class="warning">
                        <strong>⚠️ Testing Mode:</strong> This is a mock payment gateway for testing purposes. 
                        No actual payment will be processed. All payments will be automatically marked as successful.
                    </div>
                    
                    <div class="form-group">
                        <strong>Order ID:</strong> %s
                    </div>
                    
                    <div class="form-group">
                        <strong>Payment Status:</strong> Ready for Testing
                    </div>
                    
                    <div class="form-group" style="text-align: center;">
                        <button class="btn" onclick="simulateSuccess()">✅ Simulate Successful Payment</button>
                        <button class="btn btn-cancel" onclick="simulateFailure()" style="margin-left: 10px;">❌ Simulate Failed Payment</button>
                    </div>
                    
                    <div id="result" style="margin-top: 20px; text-align: center;"></div>
                </div>
                
                <script>
                    function simulateSuccess() {
                        document.getElementById('result').innerHTML = 
                            '<div style="color: green; font-weight: bold;">✅ Payment Successful (Mock)</div>' +
                            '<p>In a real integration, you would be redirected back to the merchant website.</p>';
                    }
                    
                    function simulateFailure() {
                        document.getElementById('result').innerHTML = 
                            '<div style="color: red; font-weight: bold;">❌ Payment Failed (Mock)</div>' +
                            '<p>In a real integration, you would be redirected back to the merchant website with error details.</p>';
                    }
                </script>
            </body>
            </html>
            """.formatted(orderId);
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(mockCheckoutPage);
    }

    /**
     * Mock payment status endpoint
     */
    @GetMapping("/status")
    @Operation(summary = "Get mock payment gateway status", description = "Returns the current status of mock payment mode")
    public ResponseEntity<Map<String, Object>> getMockStatus() {
        log.info("Mock payment status requested");
        
        Map<String, Object> status = Map.of(
                "mockMode", paymentGatewayConfig.shouldUseMockMode(),
                "paymentGatewayEnabled", paymentGatewayConfig.isEnabled(),
                "razorpayEnabled", paymentGatewayConfig.getRazorpay().isEnabled(),
                "stripeEnabled", paymentGatewayConfig.getStripe().isEnabled(),
                "paypalEnabled", paymentGatewayConfig.getPaypal().isEnabled(),
                "message", paymentGatewayConfig.shouldUseMockMode() ? 
                    "Mock mode is active - all payments will be simulated" : 
                    "Live payment gateways are active"
        );
        
        return ResponseEntity.ok(status);
    }
}
