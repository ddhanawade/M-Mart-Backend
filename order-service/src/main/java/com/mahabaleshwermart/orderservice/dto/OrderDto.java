package com.mahabaleshwermart.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDto {
    
    private String id;
    private String orderNumber;
    private String userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private List<OrderItemDto> items;
    private OrderAddressDto deliveryAddress;
    private OrderPaymentDto payment;
    private List<OrderTimelineDto> timeline;
    private String orderStatus;
    private String paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private int totalItems;
    private int totalQuantity;
    private String specialInstructions;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private BigDecimal refundAmount;
    private String trackingNumber;
    private String invoiceNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper methods
    public boolean isCancellable() {
        return "PENDING".equals(orderStatus) || 
               "CONFIRMED".equals(orderStatus) ||
               "PROCESSING".equals(orderStatus);
    }
    
    public boolean isRefundable() {
        return "COMPLETED".equals(paymentStatus) && 
               ("CANCELLED".equals(orderStatus) || "RETURNED".equals(orderStatus));
    }
    
	public boolean isTrackable() {
        return "SHIPPED".equals(orderStatus) || 
               "OUT_FOR_DELIVERY".equals(orderStatus);
    }
    
    /**
     * Nested DTOs
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderItemDto {
    
        private String id;
        private String productId;
        private String productName;
        private String productImage;
        private String productSku;
        private String productCategory;
        private BigDecimal unitPrice;
        private BigDecimal originalPrice;
        private String productUnit;
        private int quantity;
        private BigDecimal totalPrice;
        private BigDecimal discountAmount;
        private boolean organic;
        private boolean fresh;
        private String itemStatus;
        private String returnReason;
        private Integer returnQuantity;
        private LocalDateTime createdAt;
        
        public boolean isOnSale() {
            return originalPrice != null && originalPrice.compareTo(unitPrice) > 0;
        }
        
        public BigDecimal getSavings() {
            if (isOnSale()) {
                BigDecimal savings = originalPrice.subtract(unitPrice);
                return savings.multiply(BigDecimal.valueOf(quantity));
            }
            return BigDecimal.ZERO;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderAddressDto {
        
        private String addressType;
        private String addressName;
        private String street;
        private String city;
        private String state;
        private String pincode;
        private String landmark;
        private String contactName;
        private String contactPhone;
        private String deliveryInstructions;
        
        public String getFullAddress() {
            StringBuilder address = new StringBuilder();
            
            if (addressName != null) {
                address.append(addressName).append(", ");
            }
            
            address.append(street).append(", ");
            
            if (landmark != null && !landmark.trim().isEmpty()) {
                address.append("Near ").append(landmark).append(", ");
            }
            
            address.append(city).append(", ")
                   .append(state).append(" - ")
                   .append(pincode);
            
            return address.toString();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderPaymentDto {
        
        private String paymentMethod;
        private String paymentGateway;
        private String paymentId;
        private String transactionId;
        private String paymentReference;
        private BigDecimal paidAmount;
        private BigDecimal paymentFee;
        private String currency;
        private LocalDateTime paymentDate;
        private String failureReason;
        private String refundId;
        private BigDecimal refundAmount;
        private LocalDateTime refundDate;
        private String refundReason;
        private String cardLastFour;
        private String cardBrand;
        private String upiId;
        private String bankName;
        private String accountNumberMasked;
        
        public String getMaskedPaymentInfo() {
            return switch (paymentMethod) {
                case "CREDIT_CARD", "DEBIT_CARD" -> cardBrand + " ending in " + cardLastFour;
                case "UPI" -> "UPI: " + upiId;
                case "NET_BANKING" -> "Net Banking: " + bankName;
                case "WALLET" -> "Wallet: " + paymentGateway;
                case "CASH_ON_DELIVERY" -> "Cash on Delivery";
                case "BANK_TRANSFER" -> "Bank Transfer: " + accountNumberMasked;
                default -> paymentMethod;
            };
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderTimelineDto {
        
        private String id;
        private String eventType;
        private String title;
        private String description;
        private String orderStatus;
        private String paymentStatus;
        private String location;
        private String trackingDetails;
        private String performedBy;
        private String performedByName;
        private boolean isCustomerVisible;
        private boolean isCritical;
        private boolean notificationSent;
        private LocalDateTime createdAt;
    }
}

// Removed duplicated non-public DTO classes; consolidated into dedicated files under dto package