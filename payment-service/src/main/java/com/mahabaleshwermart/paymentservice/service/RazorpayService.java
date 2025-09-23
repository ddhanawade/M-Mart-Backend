package com.mahabaleshwermart.paymentservice.service;

import com.mahabaleshwermart.paymentservice.dto.PaymentRequest;
import com.mahabaleshwermart.paymentservice.dto.PaymentVerificationRequest;
import com.mahabaleshwermart.paymentservice.entity.Payment;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Service for Razorpay payment gateway integration
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RazorpayService {

    @Value("${payment.razorpay.key-id}")
    private String keyId;

    @Value("${payment.razorpay.key-secret}")
    private String keySecret;

    @Value("${payment.razorpay.webhook-secret}")
    private String webhookSecret;

    private RazorpayClient razorpayClient;

    private RazorpayClient getRazorpayClient() throws RazorpayException {
        if (razorpayClient == null) {
            razorpayClient = new RazorpayClient(keyId, keySecret);
        }
        return razorpayClient;
    }

    /**
     * Create Razorpay order for payment
     */
    public Order createOrder(PaymentRequest paymentRequest) throws RazorpayException {
        log.info("Creating Razorpay order for payment request: {}", paymentRequest.getOrderId());

        JSONObject orderRequest = new JSONObject();
        
        // Convert amount to paise (Razorpay expects amount in smallest currency unit)
        int amountInPaise = paymentRequest.getAmount().multiply(new BigDecimal("100")).intValue();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", paymentRequest.getCurrency());
        orderRequest.put("receipt", paymentRequest.getOrderId());

        // Add customer notes
        JSONObject notes = new JSONObject();
        notes.put("order_id", paymentRequest.getOrderId());
        notes.put("user_id", paymentRequest.getUserId());
        if (paymentRequest.getDescription() != null) {
            notes.put("description", paymentRequest.getDescription());
        }
        orderRequest.put("notes", notes);

        // Set payment capture to automatic
        orderRequest.put("payment_capture", 1);

        Order order = getRazorpayClient().orders.create(orderRequest);
        log.info("Razorpay order created successfully: {}", order.get("id").toString());

        return order;
    }

    /**
     * Verify payment signature from Razorpay
     */
    public boolean verifyPaymentSignature(PaymentVerificationRequest verificationRequest) {
        try {
            log.info("Verifying Razorpay payment signature for payment: {}", verificationRequest.getPaymentId());

            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", verificationRequest.getGatewayOrderId());
            attributes.put("razorpay_payment_id", verificationRequest.getGatewayPaymentId());
            attributes.put("razorpay_signature", verificationRequest.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(attributes, keySecret);
            log.info("Payment signature verification result: {}", isValid);

            return isValid;
        } catch (Exception e) {
            log.error("Error verifying payment signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verify webhook signature
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String expectedSignature = calculateWebhookSignature(payload);
            boolean isValid = expectedSignature.equals(signature);
            log.info("Webhook signature verification result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Calculate webhook signature for verification
     */
    private String calculateWebhookSignature(String payload) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }

    /**
     * Fetch payment details from Razorpay
     */
    public com.razorpay.Payment fetchPayment(String paymentId) throws RazorpayException {
        log.info("Fetching payment details from Razorpay: {}", paymentId);
        return getRazorpayClient().payments.fetch(paymentId);
    }

    /**
     * Create refund for a payment
     */
    public com.razorpay.Refund createRefund(String paymentId, BigDecimal amount, String reason) throws RazorpayException {
        log.info("Creating refund for payment: {} with amount: {}", paymentId, amount);

        JSONObject refundRequest = new JSONObject();
        
        if (amount != null) {
            // Convert amount to paise for partial refund
            int amountInPaise = amount.multiply(new BigDecimal("100")).intValue();
            refundRequest.put("amount", amountInPaise);
        }
        
        if (reason != null) {
            JSONObject notes = new JSONObject();
            notes.put("reason", reason);
            refundRequest.put("notes", notes);
        }

        com.razorpay.Refund refund = getRazorpayClient().payments.refund(paymentId, refundRequest);
        log.info("Refund created successfully: {}", refund.get("id").toString());

        return refund;
    }

    /**
     * Fetch refund details
     */
    public com.razorpay.Refund fetchRefund(String refundId) throws RazorpayException {
        log.info("Fetching refund details: {}", refundId);
        return getRazorpayClient().refunds.fetch(refundId);
    }

    /**
     * Get payment methods available for the merchant
     */
    public JSONObject getPaymentMethods() throws RazorpayException {
        log.info("Fetching available payment methods from Razorpay");
        // Note: Razorpay doesn't have a direct payment methods API
        // This would typically be handled via the frontend SDK
        JSONObject methods = new JSONObject();
        methods.put("card", true);
        methods.put("netbanking", true);
        methods.put("wallet", true);
        methods.put("upi", true);
        return methods;
    }

    /**
     * Convert Razorpay payment method to our enum
     */
    public Payment.PaymentMethod mapRazorpayMethod(String razorpayMethod) {
        return switch (razorpayMethod.toLowerCase()) {
            case "card" -> Payment.PaymentMethod.CREDIT_CARD; // Default to credit card
            case "upi" -> Payment.PaymentMethod.UPI;
            case "netbanking" -> Payment.PaymentMethod.NET_BANKING;
            case "wallet" -> Payment.PaymentMethod.WALLET;
            default -> Payment.PaymentMethod.CREDIT_CARD;
        };
    }

    /**
     * Extract card details from Razorpay payment response
     */
    public void extractCardDetails(Payment payment, com.razorpay.Payment razorpayPayment) {
        try {
            if (razorpayPayment.has("card")) {
                JSONObject card = (JSONObject) razorpayPayment.get("card");
                payment.setCardLastFour(card.optString("last4"));
                payment.setCardBrand(card.optString("network"));
                payment.setCardType(card.optString("type"));
            }
        } catch (Exception e) {
            log.warn("Error extracting card details: {}", e.getMessage());
        }
    }

    /**
     * Extract UPI details from Razorpay payment response
     */
    public void extractUpiDetails(Payment payment, com.razorpay.Payment razorpayPayment) {
        try {
            if (razorpayPayment.has("upi")) {
                JSONObject upi = (JSONObject) razorpayPayment.get("upi");
                payment.setUpiId(upi.optString("vpa"));
            }
        } catch (Exception e) {
            log.warn("Error extracting UPI details: {}", e.getMessage());
        }
    }

    /**
     * Extract bank details from Razorpay payment response
     */
    public void extractBankDetails(Payment payment, com.razorpay.Payment razorpayPayment) {
        try {
            if (razorpayPayment.has("bank")) {
                payment.setBankName(razorpayPayment.get("bank").toString());
            }
        } catch (Exception e) {
            log.warn("Error extracting bank details: {}", e.getMessage());
        }
    }

    /**
     * Extract wallet details from Razorpay payment response
     */
    public void extractWalletDetails(Payment payment, com.razorpay.Payment razorpayPayment) {
        try {
            if (razorpayPayment.has("wallet")) {
                payment.setWalletName(razorpayPayment.get("wallet").toString());
            }
        } catch (Exception e) {
            log.warn("Error extracting wallet details: {}", e.getMessage());
        }
    }
}
