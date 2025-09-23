package com.mahabaleshwermart.paymentservice.service;

import com.mahabaleshwermart.paymentservice.dto.PaymentRequest;
import com.mahabaleshwermart.paymentservice.dto.PaymentResponse;
import com.mahabaleshwermart.paymentservice.dto.PaymentVerificationRequest;
import com.mahabaleshwermart.paymentservice.entity.Payment;
import com.mahabaleshwermart.paymentservice.entity.PaymentTransaction;
import com.mahabaleshwermart.paymentservice.repository.PaymentRepository;
import com.mahabaleshwermart.paymentservice.repository.PaymentTransactionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Main payment service handling all payment operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final RazorpayService razorpayService;

    /**
     * Initiate a new payment
     */
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest paymentRequest) {
        log.info("Initiating payment for order: {}", paymentRequest.getOrderId());

        try {
            // Check if payment already exists for this order
            Optional<Payment> existingPayment = paymentRepository.findByOrderId(paymentRequest.getOrderId());
            if (existingPayment.isPresent()) {
                Payment payment = existingPayment.get();
                if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
                    throw new IllegalStateException("Payment already completed for order: " + paymentRequest.getOrderId());
                }
                if (payment.getStatus() == Payment.PaymentStatus.PENDING || 
                    payment.getStatus() == Payment.PaymentStatus.PROCESSING) {
                    return buildPaymentResponse(payment, "Payment already in progress", true);
                }
            }

            // Create payment entity
            Payment payment = Payment.builder()
                    .orderId(paymentRequest.getOrderId())
                    .userId(paymentRequest.getUserId())
                    .amount(paymentRequest.getAmount())
                    .currency(paymentRequest.getCurrency())
                    .paymentMethod(paymentRequest.getPaymentMethod())
                    .gatewayProvider(paymentRequest.getGatewayProvider())
                    .status(Payment.PaymentStatus.PENDING)
                    .returnUrl(paymentRequest.getReturnUrl())
                    .cancelUrl(paymentRequest.getCancelUrl())
                    .callbackUrl(paymentRequest.getCallbackUrl())
                    .build();

            // Handle different gateway providers
            switch (paymentRequest.getGatewayProvider()) {
                case RAZORPAY -> {
                    Order razorpayOrder = razorpayService.createOrder(paymentRequest);
                    payment.setGatewayOrderId(razorpayOrder.get("id"));
                    payment.setPaymentUrl("https://checkout.razorpay.com/v1/checkout.js");
                }
                case STRIPE -> {
                    // TODO: Implement Stripe integration
                    throw new UnsupportedOperationException("Stripe integration not implemented yet");
                }
                case PAYPAL -> {
                    // TODO: Implement PayPal integration
                    throw new UnsupportedOperationException("PayPal integration not implemented yet");
                }
            }

            // Save payment
            payment = paymentRepository.save(payment);

            // Create initial transaction record
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .payment(payment)
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .transactionType(PaymentTransaction.TransactionType.PAYMENT)
                    .status(PaymentTransaction.TransactionStatus.PENDING)
                    .build();
            transactionRepository.save(transaction);

            log.info("Payment initiated successfully: {}", payment.getId());
            return buildPaymentResponse(payment, "Payment initiated successfully", true);

        } catch (RazorpayException e) {
            log.error("Razorpay error during payment initiation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initiate payment: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error initiating payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initiate payment: " + e.getMessage());
        }
    }

    /**
     * Verify payment after completion
     */
    @Transactional
    public PaymentResponse verifyPayment(PaymentVerificationRequest verificationRequest) {
        log.info("Verifying payment: {}", verificationRequest.getPaymentId());

        try {
            // Find payment by ID
            Payment payment = paymentRepository.findById(verificationRequest.getPaymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + verificationRequest.getPaymentId()));

            // Verify signature based on gateway
            boolean isValid = false;
            switch (payment.getGatewayProvider()) {
                case RAZORPAY -> {
                    isValid = razorpayService.verifyPaymentSignature(verificationRequest);
                    if (isValid) {
                        // Fetch payment details from Razorpay
                        com.razorpay.Payment razorpayPayment = razorpayService.fetchPayment(verificationRequest.getGatewayPaymentId());
                        updatePaymentFromGatewayResponse(payment, razorpayPayment);
                    }
                }
                case STRIPE -> {
                    // TODO: Implement Stripe verification
                    throw new UnsupportedOperationException("Stripe verification not implemented yet");
                }
                case PAYPAL -> {
                    // TODO: Implement PayPal verification
                    throw new UnsupportedOperationException("PayPal verification not implemented yet");
                }
            }

            if (isValid) {
                // Update payment status
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setGatewayPaymentId(verificationRequest.getGatewayPaymentId());

                // Update transaction status
                List<PaymentTransaction> transactions = transactionRepository.findByPaymentId(payment.getId());
                transactions.stream()
                        .filter(t -> t.getTransactionType() == PaymentTransaction.TransactionType.PAYMENT)
                        .forEach(t -> {
                            t.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
                            t.setProcessedAt(LocalDateTime.now());
                            t.setGatewayTransactionId(verificationRequest.getGatewayPaymentId());
                        });

                paymentRepository.save(payment);
                log.info("Payment verified successfully: {}", payment.getId());
                return buildPaymentResponse(payment, "Payment verified successfully", true);
            } else {
                // Update payment status to failed
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason("Payment signature verification failed");

                // Update transaction status
                List<PaymentTransaction> transactions = transactionRepository.findByPaymentId(payment.getId());
                transactions.stream()
                        .filter(t -> t.getTransactionType() == PaymentTransaction.TransactionType.PAYMENT)
                        .forEach(t -> {
                            t.setStatus(PaymentTransaction.TransactionStatus.FAILED);
                            t.setFailureReason("Signature verification failed");
                        });

                paymentRepository.save(payment);
                log.warn("Payment verification failed: {}", payment.getId());
                return buildPaymentResponse(payment, "Payment verification failed", false);
            }

        } catch (Exception e) {
            log.error("Error verifying payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to verify payment: " + e.getMessage());
        }
    }

    /**
     * Get payment by ID
     */
    public PaymentResponse getPayment(String paymentId) {
        log.info("Fetching payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        return buildPaymentResponse(payment, "Payment retrieved successfully", true);
    }

    /**
     * Get payment by order ID
     */
    public PaymentResponse getPaymentByOrderId(String orderId) {
        log.info("Fetching payment for order: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));

        return buildPaymentResponse(payment, "Payment retrieved successfully", true);
    }

    /**
     * Get payments by user ID
     */
    public List<PaymentResponse> getPaymentsByUserId(String userId) {
        log.info("Fetching payments for user: {}", userId);

        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .map(payment -> buildPaymentResponse(payment, "Payment retrieved successfully", true))
                .toList();
    }

    /**
     * Create refund for a payment
     */
    @Transactional
    public PaymentResponse createRefund(String paymentId, BigDecimal refundAmount, String reason) {
        log.info("Creating refund for payment: {} with amount: {}", paymentId, refundAmount);

        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

            if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
                throw new IllegalStateException("Cannot refund payment that is not successful");
            }

            // Calculate total refunded amount
            Double totalRefunded = transactionRepository.getTotalRefundedAmount(paymentId);
            BigDecimal totalRefundedAmount = totalRefunded != null ? BigDecimal.valueOf(totalRefunded) : BigDecimal.ZERO;

            // Check if refund amount is valid
            BigDecimal availableForRefund = payment.getAmount().subtract(totalRefundedAmount);
            if (refundAmount.compareTo(availableForRefund) > 0) {
                throw new IllegalArgumentException("Refund amount exceeds available amount for refund");
            }

            // Create refund with gateway
            com.razorpay.Refund razorpayRefund = null;
            switch (payment.getGatewayProvider()) {
                case RAZORPAY -> {
                    razorpayRefund = razorpayService.createRefund(payment.getGatewayPaymentId(), refundAmount, reason);
                }
                case STRIPE -> {
                    // TODO: Implement Stripe refund
                    throw new UnsupportedOperationException("Stripe refund not implemented yet");
                }
                case PAYPAL -> {
                    // TODO: Implement PayPal refund
                    throw new UnsupportedOperationException("PayPal refund not implemented yet");
                }
            }

            // Create refund transaction
            PaymentTransaction refundTransaction = PaymentTransaction.builder()
                    .payment(payment)
                    .amount(refundAmount)
                    .currency(payment.getCurrency())
                    .transactionType(refundAmount.compareTo(payment.getAmount()) == 0 ? 
                            PaymentTransaction.TransactionType.REFUND : 
                            PaymentTransaction.TransactionType.PARTIAL_REFUND)
                    .status(PaymentTransaction.TransactionStatus.SUCCESS)
                    .refundReason(reason)
                    .processedAt(LocalDateTime.now())
                    .build();

            if (razorpayRefund != null) {
                refundTransaction.setGatewayTransactionId(razorpayRefund.get("id"));
            }

            transactionRepository.save(refundTransaction);

            // Update payment status if fully refunded
            BigDecimal newTotalRefunded = totalRefundedAmount.add(refundAmount);
            if (newTotalRefunded.compareTo(payment.getAmount()) == 0) {
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
            } else {
                payment.setStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
            }

            paymentRepository.save(payment);

            log.info("Refund created successfully for payment: {}", paymentId);
            return buildPaymentResponse(payment, "Refund created successfully", true);

        } catch (Exception e) {
            log.error("Error creating refund: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create refund: " + e.getMessage());
        }
    }

    /**
     * Update payment from gateway response
     */
    private void updatePaymentFromGatewayResponse(Payment payment, com.razorpay.Payment razorpayPayment) {
        try {
            // Extract payment method specific details
            switch (payment.getPaymentMethod()) {
                case CREDIT_CARD, DEBIT_CARD -> razorpayService.extractCardDetails(payment, razorpayPayment);
                case UPI -> razorpayService.extractUpiDetails(payment, razorpayPayment);
                case NET_BANKING -> razorpayService.extractBankDetails(payment, razorpayPayment);
                case WALLET -> razorpayService.extractWalletDetails(payment, razorpayPayment);
            }

            // Store gateway response (truncated for security)
            String gatewayResponse = razorpayPayment.toString();
            if (gatewayResponse.length() > 2000) {
                gatewayResponse = gatewayResponse.substring(0, 2000) + "...";
            }
            payment.setGatewayResponse(gatewayResponse);

        } catch (Exception e) {
            log.warn("Error updating payment from gateway response: {}", e.getMessage());
        }
    }

    /**
     * Build payment response DTO
     */
    private PaymentResponse buildPaymentResponse(Payment payment, String message, boolean success) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .gatewayProvider(payment.getGatewayProvider())
                .status(payment.getStatus())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .gatewayOrderId(payment.getGatewayOrderId())
                .paymentUrl(payment.getPaymentUrl())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .paymentDate(payment.getPaymentDate())
                .maskedPaymentInfo(payment.getMaskedPaymentInfo())
                .message(message)
                .success(success)
                .build();
    }
}
