package com.mahabaleshwermart.paymentservice.controller;

import com.mahabaleshwermart.paymentservice.entity.Payment;
import com.mahabaleshwermart.paymentservice.entity.PaymentTransaction;
import com.mahabaleshwermart.paymentservice.repository.PaymentRepository;
import com.mahabaleshwermart.paymentservice.repository.PaymentTransactionRepository;
import com.mahabaleshwermart.paymentservice.service.RazorpayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Webhook controller for handling payment gateway callbacks
 */
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhook Controller", description = "APIs for payment gateway webhooks")
public class WebhookController {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final RazorpayService razorpayService;

    @Operation(summary = "Razorpay webhook", description = "Handles Razorpay payment webhooks")
    @ApiResponse(responseCode = "200", description = "Webhook processed successfully")
    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        
        log.info("Received Razorpay webhook");
        
        try {
            // Verify webhook signature
            if (!razorpayService.verifyWebhookSignature(payload, signature)) {
                log.warn("Invalid webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            // Parse webhook payload
            JSONObject webhookData = new JSONObject(payload);
            String event = webhookData.getString("event");
            JSONObject paymentData = webhookData.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");

            log.info("Processing Razorpay webhook event: {}", event);

            switch (event) {
                case "payment.authorized" -> handlePaymentAuthorized(paymentData);
                case "payment.captured" -> handlePaymentCaptured(paymentData);
                case "payment.failed" -> handlePaymentFailed(paymentData);
                case "refund.created" -> handleRefundCreated(webhookData.getJSONObject("payload").getJSONObject("refund").getJSONObject("entity"));
                case "refund.processed" -> handleRefundProcessed(webhookData.getJSONObject("payload").getJSONObject("refund").getJSONObject("entity"));
                default -> log.info("Unhandled webhook event: {}", event);
            }

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Error processing Razorpay webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing failed");
        }
    }

    @Operation(summary = "Stripe webhook", description = "Handles Stripe payment webhooks")
    @ApiResponse(responseCode = "200", description = "Webhook processed successfully")
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        
        log.info("Received Stripe webhook");
        
        // TODO: Implement Stripe webhook handling
        log.warn("Stripe webhook handling not implemented yet");
        return ResponseEntity.ok("Stripe webhook received");
    }

    @Operation(summary = "PayPal webhook", description = "Handles PayPal payment webhooks")
    @ApiResponse(responseCode = "200", description = "Webhook processed successfully")
    @PostMapping("/paypal")
    public ResponseEntity<String> handlePayPalWebhook(
            @RequestBody String payload,
            @RequestHeader("PAYPAL-TRANSMISSION-ID") String transmissionId) {
        
        log.info("Received PayPal webhook");
        
        // TODO: Implement PayPal webhook handling
        log.warn("PayPal webhook handling not implemented yet");
        return ResponseEntity.ok("PayPal webhook received");
    }

    /**
     * Handle payment authorized event
     */
    private void handlePaymentAuthorized(JSONObject paymentData) {
        try {
            String razorpayPaymentId = paymentData.getString("id");
            String razorpayOrderId = paymentData.getString("order_id");

            log.info("Processing payment authorized: {}", razorpayPaymentId);

            Optional<Payment> paymentOpt = paymentRepository.findByGatewayOrderId(razorpayOrderId);
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for Razorpay order: {}", razorpayOrderId);
                return;
            }

            Payment payment = paymentOpt.get();
            payment.setGatewayPaymentId(razorpayPaymentId);
            payment.setStatus(Payment.PaymentStatus.PROCESSING);

            // Update transaction status
            List<PaymentTransaction> transactions = transactionRepository.findByPaymentId(payment.getId());
            transactions.stream()
                    .filter(t -> t.getTransactionType() == PaymentTransaction.TransactionType.PAYMENT)
                    .forEach(t -> {
                        t.setStatus(PaymentTransaction.TransactionStatus.PROCESSING);
                        t.setGatewayTransactionId(razorpayPaymentId);
                    });

            paymentRepository.save(payment);
            log.info("Payment authorized processed successfully: {}", payment.getId());

        } catch (Exception e) {
            log.error("Error processing payment authorized: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle payment captured event
     */
    private void handlePaymentCaptured(JSONObject paymentData) {
        try {
            String razorpayPaymentId = paymentData.getString("id");
            String razorpayOrderId = paymentData.getString("order_id");

            log.info("Processing payment captured: {}", razorpayPaymentId);

            Optional<Payment> paymentOpt = paymentRepository.findByGatewayOrderId(razorpayOrderId);
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for Razorpay order: {}", razorpayOrderId);
                return;
            }

            Payment payment = paymentOpt.get();
            payment.setGatewayPaymentId(razorpayPaymentId);
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setPaymentDate(LocalDateTime.now());

            // Extract payment details
            updatePaymentDetailsFromWebhook(payment, paymentData);

            // Update transaction status
            List<PaymentTransaction> transactions = transactionRepository.findByPaymentId(payment.getId());
            transactions.stream()
                    .filter(t -> t.getTransactionType() == PaymentTransaction.TransactionType.PAYMENT)
                    .forEach(t -> {
                        t.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
                        t.setGatewayTransactionId(razorpayPaymentId);
                        t.setProcessedAt(LocalDateTime.now());
                    });

            paymentRepository.save(payment);
            log.info("Payment captured processed successfully: {}", payment.getId());

        } catch (Exception e) {
            log.error("Error processing payment captured: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle payment failed event
     */
    private void handlePaymentFailed(JSONObject paymentData) {
        try {
            String razorpayPaymentId = paymentData.getString("id");
            String razorpayOrderId = paymentData.getString("order_id");

            log.info("Processing payment failed: {}", razorpayPaymentId);

            Optional<Payment> paymentOpt = paymentRepository.findByGatewayOrderId(razorpayOrderId);
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for Razorpay order: {}", razorpayOrderId);
                return;
            }

            Payment payment = paymentOpt.get();
            payment.setGatewayPaymentId(razorpayPaymentId);
            payment.setStatus(Payment.PaymentStatus.FAILED);
            
            // Extract failure reason
            if (paymentData.has("error_description")) {
                payment.setFailureReason(paymentData.getString("error_description"));
            } else if (paymentData.has("error_reason")) {
                payment.setFailureReason(paymentData.getString("error_reason"));
            }

            // Update transaction status
            List<PaymentTransaction> transactions = transactionRepository.findByPaymentId(payment.getId());
            transactions.stream()
                    .filter(t -> t.getTransactionType() == PaymentTransaction.TransactionType.PAYMENT)
                    .forEach(t -> {
                        t.setStatus(PaymentTransaction.TransactionStatus.FAILED);
                        t.setGatewayTransactionId(razorpayPaymentId);
                        t.setFailureReason(payment.getFailureReason());
                    });

            paymentRepository.save(payment);
            log.info("Payment failed processed successfully: {}", payment.getId());

        } catch (Exception e) {
            log.error("Error processing payment failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle refund created event
     */
    private void handleRefundCreated(JSONObject refundData) {
        try {
            String razorpayRefundId = refundData.getString("id");
            String razorpayPaymentId = refundData.getString("payment_id");

            log.info("Processing refund created: {}", razorpayRefundId);

            Optional<Payment> paymentOpt = paymentRepository.findByGatewayPaymentId(razorpayPaymentId);
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for Razorpay payment: {}", razorpayPaymentId);
                return;
            }

            Payment payment = paymentOpt.get();

            // Find or create refund transaction
            Optional<PaymentTransaction> refundTransactionOpt = transactionRepository.findByGatewayTransactionId(razorpayRefundId);
            if (refundTransactionOpt.isEmpty()) {
                // Create new refund transaction if not exists
                PaymentTransaction refundTransaction = PaymentTransaction.builder()
                        .payment(payment)
                        .gatewayTransactionId(razorpayRefundId)
                        .amount(payment.getAmount()) // Will be updated when processed
                        .currency(payment.getCurrency())
                        .transactionType(PaymentTransaction.TransactionType.REFUND)
                        .status(PaymentTransaction.TransactionStatus.PROCESSING)
                        .build();
                transactionRepository.save(refundTransaction);
            }

            log.info("Refund created processed successfully: {}", razorpayRefundId);

        } catch (Exception e) {
            log.error("Error processing refund created: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle refund processed event
     */
    private void handleRefundProcessed(JSONObject refundData) {
        try {
            String razorpayRefundId = refundData.getString("id");
            String razorpayPaymentId = refundData.getString("payment_id");

            log.info("Processing refund processed: {}", razorpayRefundId);

            Optional<Payment> paymentOpt = paymentRepository.findByGatewayPaymentId(razorpayPaymentId);
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for Razorpay payment: {}", razorpayPaymentId);
                return;
            }

            Payment payment = paymentOpt.get();

            // Update refund transaction
            Optional<PaymentTransaction> refundTransactionOpt = transactionRepository.findByGatewayTransactionId(razorpayRefundId);
            if (refundTransactionOpt.isPresent()) {
                PaymentTransaction refundTransaction = refundTransactionOpt.get();
                refundTransaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
                refundTransaction.setProcessedAt(LocalDateTime.now());
                transactionRepository.save(refundTransaction);

                // Update payment status
                Double totalRefunded = transactionRepository.getTotalRefundedAmount(payment.getId());
                if (totalRefunded != null && totalRefunded >= payment.getAmount().doubleValue()) {
                    payment.setStatus(Payment.PaymentStatus.REFUNDED);
                } else {
                    payment.setStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
                }
                paymentRepository.save(payment);
            }

            log.info("Refund processed successfully: {}", razorpayRefundId);

        } catch (Exception e) {
            log.error("Error processing refund processed: {}", e.getMessage(), e);
        }
    }

    /**
     * Update payment details from webhook data
     */
    private void updatePaymentDetailsFromWebhook(Payment payment, JSONObject paymentData) {
        try {
            // Extract method-specific details
            String method = paymentData.optString("method");
            payment.setPaymentMethod(razorpayService.mapRazorpayMethod(method));

            // Extract card details
            if (paymentData.has("card")) {
                JSONObject card = paymentData.getJSONObject("card");
                payment.setCardLastFour(card.optString("last4"));
                payment.setCardBrand(card.optString("network"));
                payment.setCardType(card.optString("type"));
            }

            // Extract UPI details
            if (paymentData.has("upi")) {
                JSONObject upi = paymentData.getJSONObject("upi");
                payment.setUpiId(upi.optString("vpa"));
            }

            // Extract bank details
            if (paymentData.has("bank")) {
                payment.setBankName(paymentData.optString("bank"));
            }

            // Extract wallet details
            if (paymentData.has("wallet")) {
                payment.setWalletName(paymentData.optString("wallet"));
            }

            // Store gateway response (truncated)
            String gatewayResponse = paymentData.toString();
            if (gatewayResponse.length() > 2000) {
                gatewayResponse = gatewayResponse.substring(0, 2000) + "...";
            }
            payment.setGatewayResponse(gatewayResponse);

        } catch (Exception e) {
            log.warn("Error updating payment details from webhook: {}", e.getMessage());
        }
    }
}
