package com.mahabaleshwermart.orderservice.mapper;

import com.mahabaleshwermart.orderservice.dto.OrderDto;
import com.mahabaleshwermart.orderservice.entity.*;
import com.mahabaleshwermart.orderservice.external.CartItemDto;
import com.mahabaleshwermart.orderservice.dto.CreateOrderAddressRequest;
import com.mahabaleshwermart.orderservice.dto.CreateOrderPaymentRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual mapper for Order entity and DTO conversion
 * Temporarily replacing MapStruct to resolve annotation processing conflicts
 */
@Component
public class OrderMapper {
    
    /**
     * Convert Order entity to OrderDto
     */
    public OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }
        
        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .userName(order.getUserName())
                .userEmail(order.getUserEmail())
                .userPhone(order.getUserPhone())
                .orderStatus(orderStatusToString(order.getOrderStatus()))
                .paymentStatus(paymentStatusToString(order.getPaymentStatus()))
                .subtotal(order.getSubtotal())
                .deliveryCharge(order.getDeliveryCharge())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .totalItems(order.getTotalItems())
                .totalQuantity(order.getTotalQuantity())
                .specialInstructions(order.getSpecialInstructions())
                .estimatedDelivery(order.getEstimatedDelivery())
                .actualDelivery(order.getActualDelivery())
                .items(order.getItems() != null ? order.getItems().stream().map(this::toOrderItemDto).collect(Collectors.toList()) : null)
                .deliveryAddress(order.getDeliveryAddress() != null ? toOrderAddressDto(order.getDeliveryAddress()) : null)
                .payment(order.getPayment() != null ? toOrderPaymentDto(order.getPayment()) : null)
                .timeline(order.getTimeline() != null ? order.getTimeline().stream().map(this::toOrderTimelineDto).collect(Collectors.toList()) : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert list of Order entities to list of OrderDtos
     */
    public List<OrderDto> toDtoList(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }
    
    /**
     * Convert OrderItem to OrderItemDto
     */
    public OrderDto.OrderItemDto toOrderItemDto(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        
        return OrderDto.OrderItemDto.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .productImage(orderItem.getProductImage())
                .productSku(orderItem.getProductSku())
                .productCategory(orderItem.getProductCategory())
                .unitPrice(orderItem.getUnitPrice())
                .originalPrice(orderItem.getOriginalPrice())
                .productUnit(orderItem.getProductUnit())
                .quantity(orderItem.getQuantity())
                .totalPrice(orderItem.getTotalPrice())
                .itemStatus(itemStatusToString(orderItem.getItemStatus()))
                .organic(orderItem.isOrganic())
                .fresh(orderItem.isFresh())
                .build();
    }
    
    /**
     * Convert OrderAddress to OrderAddressDto
     */
    public OrderDto.OrderAddressDto toOrderAddressDto(OrderAddress orderAddress) {
        if (orderAddress == null) {
            return null;
        }
        
        return OrderDto.OrderAddressDto.builder()
                .addressType(addressTypeToString(orderAddress.getAddressType()))
                .addressName(orderAddress.getAddressName())
                .street(orderAddress.getStreet())
                .city(orderAddress.getCity())
                .state(orderAddress.getState())
                .pincode(orderAddress.getPincode())
                .landmark(orderAddress.getLandmark())
                .contactName(orderAddress.getContactName())
                .contactPhone(orderAddress.getContactPhone())
                .deliveryInstructions(orderAddress.getDeliveryInstructions())
                .build();
    }
    
    /**
     * Convert OrderPayment to OrderPaymentDto
     */
    public OrderDto.OrderPaymentDto toOrderPaymentDto(OrderPayment orderPayment) {
        if (orderPayment == null) {
            return null;
        }
        
        return OrderDto.OrderPaymentDto.builder()
                .paymentMethod(paymentMethodToString(orderPayment.getPaymentMethod()))
                .paymentGateway(orderPayment.getPaymentGateway())
                .paymentId(orderPayment.getPaymentId())
                .transactionId(orderPayment.getTransactionId())
                .paymentReference(orderPayment.getPaymentReference())
                .paidAmount(orderPayment.getPaidAmount())
                .paymentFee(orderPayment.getPaymentFee())
                .currency(orderPayment.getCurrency())
                .paymentDate(orderPayment.getPaymentDate())
                .failureReason(orderPayment.getFailureReason())
                .refundId(orderPayment.getRefundId())
                .refundAmount(orderPayment.getRefundAmount())
                .refundDate(orderPayment.getRefundDate())
                .refundReason(orderPayment.getRefundReason())
                .cardLastFour(orderPayment.getCardLastFour())
                .cardBrand(orderPayment.getCardBrand())
                .upiId(orderPayment.getUpiId())
                .bankName(orderPayment.getBankName())
                .accountNumberMasked(orderPayment.getAccountNumberMasked())
                .build();
    }
    
    /**
     * Convert OrderTimeline to OrderTimelineDto
     */
    public OrderDto.OrderTimelineDto toOrderTimelineDto(OrderTimeline orderTimeline) {
        if (orderTimeline == null) {
            return null;
        }
        
        return OrderDto.OrderTimelineDto.builder()
                .id(orderTimeline.getId())
                .eventType(eventTypeToString(orderTimeline.getEventType()))
                .title(orderTimeline.getTitle())
                .description(orderTimeline.getDescription())
                .orderStatus(orderStatusToString(orderTimeline.getOrderStatus()))
                .paymentStatus(paymentStatusToString(orderTimeline.getPaymentStatus()))
                .location(orderTimeline.getLocation())
                .trackingDetails(orderTimeline.getTrackingDetails())
                .performedBy(orderTimeline.getPerformedBy())
                .performedByName(orderTimeline.getPerformedByName())
                .isCustomerVisible(orderTimeline.isCustomerVisible())
                .isCritical(orderTimeline.isCritical())
                .createdAt(orderTimeline.getCreatedAt())
                .build();
    }
    
    /**
     * Convert CreateOrderAddressRequest to OrderAddress
     */
    public OrderAddress toOrderAddress(CreateOrderAddressRequest request) {
        if (request == null) {
            return null;
        }
        
        return OrderAddress.builder()
                .addressType(stringToAddressType(request.getAddressType()))
                .addressName(request.getAddressName())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .landmark(request.getLandmark())
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .deliveryInstructions(request.getDeliveryInstructions())
                .build();
    }
    
    /**
     * Convert CreateOrderPaymentRequest to OrderPayment
     */
    public OrderPayment toOrderPayment(CreateOrderPaymentRequest request) {
        if (request == null) {
            return null;
        }
        
        return OrderPayment.builder()
                .paymentMethod(stringToPaymentMethod(request.getPaymentMethod()))
                .paymentGateway(request.getPaymentGateway())
                .upiId(request.getUpiId())
                .build();
    }
    
    /**
     * Convert CartItemDto to OrderItem
     */
    public OrderItem toOrderItem(CartItemDto cartItem, Order order) {
        if (cartItem == null) {
            return null;
        }
        
        return OrderItem.builder()
                .order(order)
                .productId(cartItem.productId())
                .productName(cartItem.productName())
                .productImage(cartItem.productImage())
                .productSku(cartItem.productSku())
                .productCategory(cartItem.productCategory())
                .unitPrice(cartItem.productPrice())
                .originalPrice(cartItem.originalPrice())
                .productUnit(cartItem.productUnit())
                .quantity(cartItem.quantity())
                .totalPrice(cartItem.totalPrice())
                .organic(cartItem.organic())
                .fresh(cartItem.fresh())
                .itemStatus(OrderItem.ItemStatus.CONFIRMED)
                .build();
    }
    
    // Enum conversion methods
    
    public String orderStatusToString(Order.OrderStatus status) {
        return status != null ? status.name() : null;
    }
    
    public String paymentStatusToString(Order.PaymentStatus status) {
        return status != null ? status.name() : null;
    }
    
    public String itemStatusToString(OrderItem.ItemStatus status) {
        return status != null ? status.name() : null;
    }
    
    public String addressTypeToString(OrderAddress.AddressType type) {
        return type != null ? type.name() : null;
    }
    
    public String paymentMethodToString(OrderPayment.PaymentMethod method) {
        return method != null ? method.name() : null;
    }
    
    public String eventTypeToString(OrderTimeline.EventType type) {
        return type != null ? type.name() : null;
    }
    
    public OrderAddress.AddressType stringToAddressType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }
        try {
            return OrderAddress.AddressType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OrderAddress.AddressType.OTHER;
        }
    }
    
    public OrderPayment.PaymentMethod stringToPaymentMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            return null;
        }
        try {
            return OrderPayment.PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OrderPayment.PaymentMethod.CASH_ON_DELIVERY;
        }
    }
}