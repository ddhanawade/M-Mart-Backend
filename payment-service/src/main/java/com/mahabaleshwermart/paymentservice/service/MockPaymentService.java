package com.mahabaleshwermart.paymentservice.service;

import com.mahabaleshwermart.paymentservice.dto.PaymentRequest;
import com.mahabaleshwermart.paymentservice.dto.PaymentVerificationRequest;
import com.mahabaleshwermart.paymentservice.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mock payment service for testing without actual payment gateway integration
 */
@Service
@Slf4j
public class MockPaymentService {

    /**
     * Create a mock payment order
     */
    public MockPaymentOrder createMockOrder(PaymentRequest paymentRequest) {
        log.info("Creating mock payment order for order: {}", paymentRequest.getOrderId());
        
        String mockOrderId = "mock_order_" + UUID.randomUUID().toString().substring(0, 8);
        String mockPaymentUrl = "http://localhost:8086/api/payments/mock/checkout/" + mockOrderId;
        
        return MockPaymentOrder.builder()
                .id(mockOrderId)
                .amount(paymentRequest.getAmount())
                .currency(paymentRequest.getCurrency())
                .paymentUrl(mockPaymentUrl)
                .status("created")
                .build();
    }

    /**
     * Simulate payment verification (always returns success for testing)
     */
    public boolean verifyMockPayment(PaymentVerificationRequest verificationRequest) {
        log.info("Verifying mock payment: {}", verificationRequest.getPaymentId());
        
        // In mock mode, we always return true for verification
        // This allows testing the complete order flow without actual payment processing
        return true;
    }

    /**
     * Update payment with mock gateway response
     */
    public void updatePaymentWithMockResponse(Payment payment) {
        log.info("Updating payment with mock response: {}", payment.getId());
        
        // Set mock gateway details
        payment.setGatewayPaymentId("mock_pay_" + UUID.randomUUID().toString().substring(0, 8));
        payment.setGatewayResponse("Mock payment response - payment processed successfully in test mode");
        
        // Set mock payment method details for testing
        payment.setCardLastFour("1234");
        payment.setCardBrand("VISA");
        payment.setCardType("CREDIT");
        
        // Set payment date
        payment.setPaymentDate(LocalDateTime.now());
    }

    /**
     * Create mock refund
     */
    public MockRefund createMockRefund(String paymentId, java.math.BigDecimal refundAmount, String reason) {
        log.info("Creating mock refund for payment: {} with amount: {}", paymentId, refundAmount);
        
        String mockRefundId = "mock_rfnd_" + UUID.randomUUID().toString().substring(0, 8);
        
        return MockRefund.builder()
                .id(mockRefundId)
                .paymentId(paymentId)
                .amount(refundAmount)
                .status("processed")
                .reason(reason)
                .build();
    }

    /**
     * Mock payment order response
     */
    @lombok.Builder
    @lombok.Data
    public static class MockPaymentOrder {
        private String id;
        private java.math.BigDecimal amount;
        private String currency;
        private String paymentUrl;
        private String status;
    }

    /**
     * Mock refund response
     */
    @lombok.Builder
    @lombok.Data
    public static class MockRefund {
        private String id;
        private String paymentId;
        private java.math.BigDecimal amount;
        private String status;
        private String reason;
    }
}
