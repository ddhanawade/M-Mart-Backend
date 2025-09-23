package com.mahabaleshwermart.orderservice.service;

import com.mahabaleshwermart.common.dto.PageResponse;
import com.mahabaleshwermart.common.exception.BusinessException;
import com.mahabaleshwermart.common.exception.ResourceNotFoundException;
import com.mahabaleshwermart.orderservice.dto.ApiResponse;
import com.mahabaleshwermart.orderservice.dto.OrderDto;
import com.mahabaleshwermart.orderservice.entity.*;
import com.mahabaleshwermart.orderservice.dto.CreateOrderRequest;
import com.mahabaleshwermart.orderservice.mapper.OrderMapper;
import com.mahabaleshwermart.orderservice.repository.OrderRepository;
import com.mahabaleshwermart.orderservice.external.CartServiceClient;
import com.mahabaleshwermart.orderservice.external.CartSummaryDto;
import com.mahabaleshwermart.orderservice.external.CartItemDto;
import com.mahabaleshwermart.orderservice.external.UserServiceClient;
import com.mahabaleshwermart.orderservice.external.UserDto;
import com.mahabaleshwermart.orderservice.client.PaymentServiceClient;
import com.mahabaleshwermart.orderservice.dto.payment.PaymentRequest;
import com.mahabaleshwermart.orderservice.dto.payment.PaymentResponse;
import com.mahabaleshwermart.orderservice.dto.payment.PaymentVerificationRequest;
import com.mahabaleshwermart.orderservice.dto.payment.RefundRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Service
 * Handles order processing, payment integration, and order lifecycle management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;
    private final CartServiceClient cartServiceClient;
    private final UserServiceClient userServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.18); // 18% GST
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = BigDecimal.valueOf(500);
    private static final BigDecimal STANDARD_DELIVERY_CHARGE = BigDecimal.valueOf(50);
    
    /**
     * Create order from cart
     */
    @Transactional
    public OrderDto createOrderFromCart(String userId, CreateOrderRequest request) {
        log.info("Creating order from cart for user: {}", userId);
        
        // Create simplified order without external service dependencies
        Order order = createSimplifiedOrder(userId, request);
        order = orderRepository.save(order);
        
        // Add initial timeline event (simplified)
        if (order.getTimeline() != null) {
            OrderTimeline orderPlacedEvent = OrderTimeline.builder()
                    .order(order)
                    .eventType(OrderTimeline.EventType.ORDER_PLACED)
                    .title("Order Placed")
                    .description("Your order has been successfully placed")
                    .orderStatus(Order.OrderStatus.PENDING)
                    .performedBy(userId)
                    .isCustomerVisible(true)
                    .isCritical(true)
                    .build();
            order.getTimeline().add(orderPlacedEvent);
        }
        
        // Process payment (simplified for now)
        if (!"CASH_ON_DELIVERY".equals(request.getPayment().getPaymentMethod())) {
            // Set payment as completed for testing purposes
            order.setPaymentStatus(Order.PaymentStatus.COMPLETED);
            order.getPayment().setPaymentDate(LocalDateTime.now());
            order.getPayment().setPaidAmount(order.getTotalAmount());
        }
        
        // Send order confirmation notification
        try {
            notificationService.sendOrderConfirmation(order);
        } catch (Exception e) {
            log.warn("Failed to send order confirmation notification for order: {}", order.getOrderNumber(), e);
        }
        
        // Save the order with timeline
        order = orderRepository.save(order);
        
        // Clear user's cart after successful order creation
        try {
            cartServiceClient.clearUserCart(userId);
        } catch (Exception e) {
            log.warn("Failed to clear cart for user {} after order creation: {}", userId, e.getMessage());
        }

        log.info("Order created successfully: {}", order.getOrderNumber());
        return orderMapper.toDto(order);
    }
    
    /**
     * Create simplified order with mock data (public method for testing)
     */
    @Transactional
    public OrderDto createSimplifiedOrderPublic(CreateOrderRequest request) {
        log.info("Creating simplified order using dynamic data");
        
        // Use a default user ID for simplified orders
        String defaultUserId = "test-user-123";
        Order order = createSimplifiedOrder(defaultUserId, request);
        
        // Send notification
        try {
            notificationService.sendOrderConfirmation(order);
        } catch (Exception e) {
            log.warn("Failed to send order confirmation notification for order: {}", order.getOrderNumber(), e);
        }
        
        // Save the order
        order = orderRepository.save(order);
        
        log.info("Simplified order created successfully: {}", order.getOrderNumber());
        return orderMapper.toDto(order);
    }
    
    /**
     * Get order by ID
     */
    @Cacheable(value = "order", key = "#orderId")
    @Transactional(readOnly = true)
    public OrderDto getOrderById(String orderId) {
        log.info("Fetching order by ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
                
        return orderMapper.toDto(order);
    }
    
    /**
     * Get order by order number
     */
    @Cacheable(value = "order-number", key = "#orderNumber")
    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        log.info("Fetching order by order number: {}", orderNumber);
        
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
                
        return orderMapper.toDto(order);
    }
    
    /**
     * Get orders by user ID
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderDto> getUserOrders(String userId, Pageable pageable) {
        log.info("Fetching orders for user: {}", userId);
        
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<OrderDto> orderDtos = orderMapper.toDtoList(orders.getContent());
        
        return PageResponse.of(
            orderDtos,
            orders.getNumber(),
            orders.getSize(),
            orders.getTotalElements(),
            orders.getTotalPages()
        );
    }
    
    /**
     * Get orders by status
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderDto> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        log.info("Fetching orders by status: {}", status);
        
        Page<Order> orders = orderRepository.findByOrderStatusOrderByCreatedAtDesc(status, pageable);
        List<OrderDto> orderDtos = orderMapper.toDtoList(orders.getContent());
        
        return PageResponse.of(
            orderDtos,
            orders.getNumber(),
            orders.getSize(),
            orders.getTotalElements(),
            orders.getTotalPages()
        );
    }
    
    /**
     * Update order status
     */
    @CacheEvict(value = {"order", "order-number"}, allEntries = true)
    @Transactional
    public OrderDto updateOrderStatus(String orderId, Order.OrderStatus newStatus, 
                                     String notes, String performedBy) {
        log.info("Updating order {} status to {}", orderId, newStatus);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        Order.OrderStatus oldStatus = order.getOrderStatus();
        
        // Validate status transition
        validateStatusTransition(oldStatus, newStatus);
        
        // Update order status
        order.setOrderStatus(newStatus);
        
        // Add timeline event
        OrderTimeline statusEvent = createStatusUpdateEvent(order, oldStatus, newStatus, notes, performedBy);
        order.getTimeline().add(statusEvent);
        
        // Handle specific status changes
        handleStatusChange(order, newStatus, performedBy);
        
        order = orderRepository.save(order);
        
        // Send status update notification
        try {
            notificationService.sendOrderStatusUpdate(order, oldStatus, newStatus);
        } catch (Exception e) {
            log.warn("Failed to send order status update notification for order: {}", order.getOrderNumber(), e);
        }
        
        log.info("Order status updated successfully: {} -> {}", oldStatus, newStatus);
        return orderMapper.toDto(order);
    }
    
    /**
     * Cancel order
     */
    @CacheEvict(value = {"order", "order-number"}, allEntries = true)
    @Transactional
    public OrderDto cancelOrder(String orderId, String reason, String performedBy) {
        log.info("Cancelling order: {} with reason: {}", orderId, reason);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        // Validate cancellation
        if (!order.isCancellable()) {
            throw new BusinessException("Order cannot be cancelled in current status: " + order.getOrderStatus());
        }
        
        // Update order
        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);
        
        // Add timeline event
        OrderTimeline cancelEvent = OrderTimeline.orderCancelled(order, reason, performedBy);
        order.getTimeline().add(cancelEvent);
        
        // Process refund if payment was completed
        if (order.getPaymentStatus() == Order.PaymentStatus.COMPLETED) {
            processRefund(order, order.getTotalAmount(), "Order cancellation");
        }
        
        order = orderRepository.save(order);
        
        // Send cancellation notification
        try {
            notificationService.sendOrderCancellation(order, reason);
        } catch (Exception e) {
            log.warn("Failed to send order cancellation notification for order: {}", order.getOrderNumber(), e);
        }
        
        log.info("Order cancelled successfully: {}", orderId);
        return orderMapper.toDto(order);
    }
    
    /**
     * Track order
     */
    @Transactional(readOnly = true)
    public OrderDto trackOrder(String orderNumber) {
        log.info("Tracking order: {}", orderNumber);
        
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        
        if (!order.isTrackable()) {
            throw new BusinessException("Order is not yet trackable. Current status: " + order.getOrderStatus());
        }
        
        return orderMapper.toDto(order);
    }
    
    /**
     * Search orders
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderDto> searchOrders(String searchTerm, Pageable pageable) {
        log.info("Searching orders with term: {}", searchTerm);
        
        Page<Order> orders = orderRepository.searchOrders(searchTerm, pageable);
        List<OrderDto> orderDtos = orderMapper.toDtoList(orders.getContent());
        
        return PageResponse.of(
            orderDtos,
            orders.getNumber(),
            orders.getSize(),
            orders.getTotalElements(),
            orders.getTotalPages()
        );
    }
    
    /**
     * Get order statistics
     */
    @Cacheable(value = "order-stats", key = "#userId + '_' + #days")
    @Transactional(readOnly = true)
    public OrderStatistics getOrderStatistics(String userId, int days) {
        log.info("Getting order statistics for user: {} for last {} days", userId, days);
        
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        
        long totalOrders = orderRepository.countByUserId(userId);
        BigDecimal totalSpent = orderRepository.getTotalOrderValueByUser(userId, Order.OrderStatus.CANCELLED);
        
        BigDecimal average = BigDecimal.ZERO;
        if (totalOrders > 0) {
            average = totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP);
        }
        return OrderStatistics.builder()
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .averageOrderValue(average)
                .build();
    }
    
    // Private helper methods
    
    private Order createSimplifiedOrder(String userId, CreateOrderRequest request) {
        log.info("Creating simplified order for user: {}", userId);
        
        // Fetch user profile information from user service
        UserDto userProfile = null;
        try {
            ApiResponse<UserDto> userResponse = userServiceClient.getUserById(userId);
            if (userResponse != null && userResponse.isSuccess() && userResponse.getData() != null) {
                userProfile = userResponse.getData();
                log.info("Successfully fetched user profile for user: {}: {}", userId, userProfile);
            } else {
                log.warn("User service returned empty or unsuccessful response for user: {}", userId);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch user profile for user: {}. Error: {}", userId, e.getMessage());
        }
        
        // Resolve user details from request if available
        OrderAddress deliveryAddress = orderMapper.toOrderAddress(request.getDeliveryAddress());
        // Use user profile data if available, otherwise fall back to delivery address or defaults
        String resolvedUserName = userProfile != null && userProfile.name() != null
                ? userProfile.name()
                : (deliveryAddress != null && deliveryAddress.getContactName() != null
                    ? deliveryAddress.getContactName()
                    : userId);
        String resolvedUserPhone = userProfile != null && userProfile.phone() != null
                ? userProfile.phone()
                : (deliveryAddress != null ? deliveryAddress.getContactPhone() : null);
        String resolvedUserEmail = userProfile != null && userProfile.email() != null
                ? userProfile.email()
                : ((userId != null && userId.contains("@"))
                    ? userId
                    : (resolvedUserName != null ? resolvedUserName.toLowerCase().replaceAll("\\s+", ".") : "user") + "@unknown.local");
        
        // Fetch and validate user's cart
        CartSummaryDto cartSummary = null;
        try {
            // First try user cart (should be merged after login)
            ApiResponse<CartSummaryDto> cartResponse = cartServiceClient.validateCart(userId);
            if (cartResponse != null && cartResponse.isSuccess()) {
                cartSummary = cartResponse.getData();
                log.info("Cart validation successful for user {}: {} items", userId, 
                        cartSummary != null && cartSummary.items() != null ? cartSummary.items().size() : 0);
            }
        } catch (Exception ex) {
            log.warn("Cart validation error for user {}: {}", userId, ex.getMessage());
        }

        // If user cart is empty or null, try guest cart using propagated header
        if (cartSummary == null || cartSummary.items() == null || cartSummary.items().isEmpty()) {
            String guestSessionId = null;
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                HttpServletRequest req = servletAttrs.getRequest();
                guestSessionId = req.getHeader("X-Guest-Session");
            }
            if (guestSessionId != null && !guestSessionId.isBlank()) {
                try {
                    log.info("User cart empty; attempting guest cart for session {}", guestSessionId);
                    ApiResponse<CartSummaryDto> guestCartResponse = cartServiceClient.validateCartGuest(guestSessionId, "true");
                    if (guestCartResponse != null && guestCartResponse.isSuccess()) {
                        cartSummary = guestCartResponse.getData();
                    }
                } catch (Exception e2) {
                    log.warn("Guest cart validation failed for session {}: {}", guestSessionId, e2.getMessage());
                }
            }
        }

        // As a last fallback, try fetching user cart without validation
        if (cartSummary == null || cartSummary.items() == null || cartSummary.items().isEmpty()) {
            try {
                ApiResponse<CartSummaryDto> cartResponse = cartServiceClient.getUserCart(userId);
                if (cartResponse != null && cartResponse.isSuccess()) {
                    cartSummary = cartResponse.getData();
                }
            } catch (Exception e3) {
                log.error("Failed to fetch cart for user {}: {}", userId, e3.getMessage(), e3);
            }
        }

        if (cartSummary == null || cartSummary.items() == null || cartSummary.items().isEmpty()) {
            // If guest session exists, fail over by clearing stale guest cart reference
            RequestAttributes attrs2 = RequestContextHolder.getRequestAttributes();
            if (attrs2 instanceof ServletRequestAttributes servletAttrs2) {
                String guestSessionId2 = servletAttrs2.getRequest().getHeader("X-Guest-Session");
                if (guestSessionId2 != null && !guestSessionId2.isBlank()) {
                    try {
                        cartServiceClient.clearGuestCart(guestSessionId2);
                    } catch (Exception ignore) {}
                }
            }
            throw new BusinessException("Cart is empty. Please add items before placing an order.");
        }
        
        // Compute amounts dynamically
        BigDecimal subtotal = cartSummary.subtotal() != null ? cartSummary.subtotal() : BigDecimal.ZERO;
        BigDecimal discountAmount = cartSummary.totalSavings() != null ? cartSummary.totalSavings() : BigDecimal.ZERO;
        BigDecimal taxableBase = subtotal.subtract(discountAmount);
        if (taxableBase.compareTo(BigDecimal.ZERO) < 0) taxableBase = BigDecimal.ZERO;
        BigDecimal taxAmount = taxableBase.multiply(TAX_RATE);
        BigDecimal deliveryCharge = subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0 ? BigDecimal.ZERO : STANDARD_DELIVERY_CHARGE;
        int totalItems = cartSummary.totalItems();
        int totalQuantity = cartSummary.totalQuantity();
        
        // Payment
        OrderPayment payment = orderMapper.toOrderPayment(request.getPayment());
        
        // Build order
        Order order = Order.builder()
                .userId(userId)
                .userName(resolvedUserName)
                .userEmail(resolvedUserEmail)
                .userPhone(resolvedUserPhone)
                .specialInstructions(request.getSpecialInstructions())
                .deliveryAddress(deliveryAddress)
                .payment(payment)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .deliveryCharge(deliveryCharge)
                .discountAmount(discountAmount)
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .build();
        
        // Initialize timeline
        order.setTimeline(new ArrayList<>());
        
        // Map cart items → order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemDto ci : cartSummary.items()) {
            orderItems.add(orderMapper.toOrderItem(ci, order));
        }
        order.setItems(orderItems);
        
        log.info("Created order with {} items. Subtotal: {}, Tax: {}, Delivery: {}, Discount: {}",
                orderItems.size(), subtotal, taxAmount, deliveryCharge, discountAmount);
        
        return order;
    }
    
    
    // Removed createOrderFromCartItems method - not needed for simplified implementation
    
    // Removed processPayment method - using simplified payment logic in createOrderFromCart
    
    private void processRefund(Order order, BigDecimal refundAmount, String reason) {
        // Simplified refund processing for testing
        log.info("Processing refund for order: {} amount: {} reason: {}", order.getOrderNumber(), refundAmount, reason);
        order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
        order.setRefundAmount(refundAmount);
        order.getPayment().setRefundAmount(refundAmount);
        order.getPayment().setRefundDate(LocalDateTime.now());
        order.getPayment().setRefundReason(reason);
        log.info("Refund processed successfully for order: {}", order.getOrderNumber());
    }
    
    private void validateStatusTransition(Order.OrderStatus from, Order.OrderStatus to) {
        // Define valid status transitions
        boolean validTransition = switch (from) {
            case PENDING -> to == Order.OrderStatus.CONFIRMED || to == Order.OrderStatus.CANCELLED;
            case CONFIRMED -> to == Order.OrderStatus.PROCESSING || to == Order.OrderStatus.CANCELLED;
            case PROCESSING -> to == Order.OrderStatus.PACKED || to == Order.OrderStatus.CANCELLED;
            case PACKED -> to == Order.OrderStatus.SHIPPED;
            case SHIPPED -> to == Order.OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> to == Order.OrderStatus.DELIVERED;
            case DELIVERED -> to == Order.OrderStatus.RETURNED;
            default -> false;
        };
        
        if (!validTransition) {
            throw new BusinessException("Invalid status transition from " + from + " to " + to);
        }
    }
    
    private OrderTimeline createStatusUpdateEvent(Order order, Order.OrderStatus oldStatus, 
                                                Order.OrderStatus newStatus, String notes, String performedBy) {
        return OrderTimeline.builder()
                .order(order)
                .eventType(mapStatusToEventType(newStatus))
                .title(getStatusTitle(newStatus))
                .description(notes != null ? notes : getStatusDescription(newStatus))
                .orderStatus(newStatus)
                .performedBy(performedBy)
                .performedByName(getPerformedByName(performedBy))
                .isCustomerVisible(true)
                .isCritical(isCriticalStatus(newStatus))
                .build();
    }
    
    private void handleStatusChange(Order order, Order.OrderStatus newStatus, String performedBy) {
        switch (newStatus) {
            case CONFIRMED -> order.setOrderStatus(Order.OrderStatus.CONFIRMED);
            case SHIPPED -> {
                if (order.getTrackingNumber() == null) {
                    order.setTrackingNumber(generateTrackingNumber());
                }
            }
            case DELIVERED -> order.setActualDelivery(LocalDateTime.now());
        }
    }
    
    private String generateTrackingNumber() {
        return "TRK-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    // Helper methods for status mapping
    private OrderTimeline.EventType mapStatusToEventType(Order.OrderStatus status) {
        return switch (status) {
            case PENDING -> OrderTimeline.EventType.INTERNAL_NOTE;
            case CONFIRMED -> OrderTimeline.EventType.ORDER_CONFIRMED;
            case PROCESSING -> OrderTimeline.EventType.ORDER_PROCESSING;
            case PACKED -> OrderTimeline.EventType.ORDER_PACKED;
            case SHIPPED -> OrderTimeline.EventType.ORDER_SHIPPED;
            case OUT_FOR_DELIVERY -> OrderTimeline.EventType.OUT_FOR_DELIVERY;
            case DELIVERED -> OrderTimeline.EventType.ORDER_DELIVERED;
            case CANCELLED -> OrderTimeline.EventType.ORDER_CANCELLED;
            case RETURNED -> OrderTimeline.EventType.ORDER_RETURNED;
            case REFUNDED -> OrderTimeline.EventType.INTERNAL_NOTE;
        };
    }
    
    private String getStatusTitle(Order.OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "Order Confirmed";
            case PROCESSING -> "Order Processing";
            case PACKED -> "Order Packed";
            case SHIPPED -> "Order Shipped";
            case OUT_FOR_DELIVERY -> "Out for Delivery";
            case DELIVERED -> "Order Delivered";
            case CANCELLED -> "Order Cancelled";
            case RETURNED -> "Order Returned";
            default -> "Status Updated";
        };
    }
    
    private String getStatusDescription(Order.OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "Your order has been confirmed and will be processed soon";
            case PROCESSING -> "Your order is being prepared";
            case PACKED -> "Your order has been packed and ready for shipment";
            case SHIPPED -> "Your order has been shipped";
            case OUT_FOR_DELIVERY -> "Your order is out for delivery";
            case DELIVERED -> "Your order has been delivered successfully";
            case CANCELLED -> "Your order has been cancelled";
            case RETURNED -> "Your order has been returned";
            default -> "Order status updated";
        };
    }
    
    private String getPerformedByName(String performedBy) {
        if ("SYSTEM".equals(performedBy)) {
            return "System";
        }
        // Could fetch user name from user service
        return "Staff";
    }
    
    /**
     * Initiate payment for an order
     */
    @Transactional
    public PaymentResponse initiatePayment(Long orderId, String paymentMethod, String gatewayProvider) {
        log.info("Initiating payment for order: {}", orderId);
        
        Order order = orderRepository.findById(String.valueOf(orderId))
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
            
        // Validate order status
        if (order.getOrderStatus() != Order.OrderStatus.PENDING) {
            throw new BusinessException("Payment can only be initiated for pending orders");
        }
        
        // Get user details for payment
        UserDto user = null;
        try {
            ApiResponse<UserDto> userResponse = userServiceClient.getUserById(order.getUserId());
            if (userResponse != null && userResponse.isSuccess() && userResponse.getData() != null) {
                user = userResponse.getData();
            } else {
                throw new BusinessException("Failed to fetch user details for payment");
            }
        } catch (Exception e) {
            log.error("Error fetching user details for payment: {}", e.getMessage());
            throw new BusinessException("Failed to fetch user details for payment: " + e.getMessage());
        }
        
        // Create payment request
        PaymentRequest paymentRequest = PaymentRequest.builder()
            .orderId(orderId)
            .userId(Long.valueOf(order.getUserId()))
            .amount(order.getTotalAmount())
            .currency("INR")
            .paymentMethod(paymentMethod)
            .gatewayProvider(gatewayProvider)
            .description("Payment for Order #" + order.getOrderNumber())
            .customerEmail(user.email())
            .customerPhone(user.phone())
            .customerName(user.name())
            .orderNumber(order.getOrderNumber())
            .successUrl("http://localhost:3000/payment/success")
            .failureUrl("http://localhost:3000/payment/failure")
            .cancelUrl("http://localhost:3000/payment/cancel")
            .notes("Order payment for " + order.getOrderNumber())
            .build();
            
        try {
            // Call payment service to initiate payment
            var response = paymentServiceClient.initiatePayment(paymentRequest);
            PaymentResponse paymentResponse = response.getBody();
            
            if (paymentResponse != null && "SUCCESS".equals(paymentResponse.getStatus())) {
                // Update order payment details
                updateOrderPaymentDetails(order, paymentResponse, paymentMethod);
                orderRepository.save(order);
                
                log.info("Payment initiated successfully for order: {}, payment ID: {}", 
                    orderId, paymentResponse.getPaymentId());
            }
            
            return paymentResponse;
            
        } catch (Exception e) {
            log.error("Failed to initiate payment for order: {}", orderId, e);
            throw new BusinessException("Failed to initiate payment: " + e.getMessage());
        }
    }
    
    /**
     * Verify payment completion
     */
    @Transactional
    public PaymentResponse verifyPayment(Long orderId, PaymentVerificationRequest verificationRequest) {
        log.info("Verifying payment for order: {}", orderId);
        
        Order order = orderRepository.findById(String.valueOf(orderId))
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
            
        try {
            // Call payment service to verify payment
            var response = paymentServiceClient.verifyPayment(verificationRequest);
            PaymentResponse paymentResponse = response.getBody();
            
            if (paymentResponse != null) {
                // Update order based on payment status
                updateOrderAfterPaymentVerification(order, paymentResponse);
                orderRepository.save(order);
                
                // Send notification based on payment status
                if ("SUCCESS".equals(paymentResponse.getStatus())) {
                    // Send notification - method signature may need adjustment
                    // notificationService.sendOrderConfirmationEmail(order.getId());
                    log.info("Payment verified successfully for order: {}", orderId);
                } else {
                    log.warn("Payment verification failed for order: {}, status: {}", 
                        orderId, paymentResponse.getStatus());
                }
            }
            
            return paymentResponse;
            
        } catch (Exception e) {
            log.error("Failed to verify payment for order: {}", orderId, e);
            throw new BusinessException("Failed to verify payment: " + e.getMessage());
        }
    }
    
    /**
     * Process refund for an order
     */
    @Transactional
    public PaymentResponse processRefund(Long orderId, RefundRequest refundRequest) {
        log.info("Processing refund for order: {}", orderId);
        
        Order order = orderRepository.findById(String.valueOf(orderId))
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
            
        // Validate order can be refunded
        if (order.getPayment() == null || !order.getPayment().isCompleted()) {
            throw new BusinessException("Order payment must be completed before refund can be processed");
        }
        
        try {
            // Get payment details from payment service
            var paymentResponse = paymentServiceClient.getPaymentByOrderId(orderId);
            PaymentResponse payment = paymentResponse.getBody();
            
            if (payment == null || payment.getPaymentId() == null) {
                throw new BusinessException("Payment details not found for order");
            }
            
            // Call payment service to create refund
            var response = paymentServiceClient.createRefund(payment.getPaymentId(), refundRequest);
            PaymentResponse refundResponse = response.getBody();
            
            if (refundResponse != null && "REFUND_INITIATED".equals(refundResponse.getStatus())) {
                // Update order status and payment details
                updateOrderAfterRefund(order, refundResponse);
                orderRepository.save(order);
                
                log.info("Refund initiated successfully for order: {}", orderId);
            }
            
            return refundResponse;
            
        } catch (Exception e) {
            log.error("Failed to process refund for order: {}", orderId, e);
            throw new BusinessException("Failed to process refund: " + e.getMessage());
        }
    }
    
    /**
     * Update order payment details after payment initiation
     */
    private void updateOrderPaymentDetails(Order order, PaymentResponse paymentResponse, String paymentMethod) {
        if (order.getPayment() == null) {
            order.setPayment(new OrderPayment());
        }
        
        OrderPayment payment = order.getPayment();
        payment.setPaymentGateway(paymentResponse.getGatewayProvider());
        payment.setPaymentId(paymentResponse.getGatewayOrderId());
        payment.setPaymentMethod(OrderPayment.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
        order.setPaymentStatus(Order.PaymentStatus.PROCESSING);
        payment.setPaidAmount(paymentResponse.getAmount());
        payment.setCurrency(paymentResponse.getCurrency());
    }
    
    /**
     * Update order after payment verification
     */
    private void updateOrderAfterPaymentVerification(Order order, PaymentResponse paymentResponse) {
        OrderPayment payment = order.getPayment();
        
        if ("SUCCESS".equals(paymentResponse.getStatus())) {
            order.setPaymentStatus(Order.PaymentStatus.COMPLETED);
            payment.setPaymentId(paymentResponse.getGatewayPaymentId());
            payment.setTransactionId(paymentResponse.getGatewayTransactionId());
            payment.setPaymentDate(paymentResponse.getPaymentCompletedAt());
            
            // Update payment method details
            if (paymentResponse.getCardLastFour() != null) {
                payment.setCardLastFour(paymentResponse.getCardLastFour());
                payment.setCardBrand(paymentResponse.getCardBrand());
            }
            if (paymentResponse.getUpiId() != null) {
                payment.setUpiId(paymentResponse.getUpiId());
            }
            if (paymentResponse.getBankName() != null) {
                payment.setBankName(paymentResponse.getBankName());
            }
            // Note: Wallet name not supported in current OrderPayment entity
            
            // Update order status to confirmed
            order.setOrderStatus(Order.OrderStatus.CONFIRMED);
            
            // Add timeline event
            addTimelineEvent(order, OrderTimeline.EventType.PAYMENT_COMPLETED, 
                "Payment completed successfully", "SYSTEM");
                
        } else {
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
            payment.setFailureReason(paymentResponse.getErrorMessage());
            
            // Add timeline event
            addTimelineEvent(order, OrderTimeline.EventType.PAYMENT_FAILED, 
                "Payment failed: " + paymentResponse.getErrorMessage(), "SYSTEM");
        }
    }
    
    /**
     * Update order after refund processing
     */
    private void updateOrderAfterRefund(Order order, PaymentResponse refundResponse) {
        OrderPayment payment = order.getPayment();
        
        if ("REFUND_INITIATED".equals(refundResponse.getStatus())) {
            order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
            payment.setRefundAmount(refundResponse.getAmount());
            payment.setRefundDate(LocalDateTime.now());
            
            // Update order status
            if (order.getOrderStatus() != Order.OrderStatus.CANCELLED) {
                order.setOrderStatus(Order.OrderStatus.RETURNED);
            }
            
            // Add timeline event
            addTimelineEvent(order, OrderTimeline.EventType.REFUND_INITIATED, 
                "Refund initiated for amount: ₹" + refundResponse.getAmount(), "SYSTEM");
        }
    }
    
    /**
     * Add timeline event to order
     */
    private void addTimelineEvent(Order order, OrderTimeline.EventType eventType, String description, String performedBy) {
        if (order.getTimeline() == null) {
            order.setTimeline(new ArrayList<>());
        }
        
        OrderTimeline timelineEvent = OrderTimeline.builder()
            .order(order)
            .eventType(eventType)
            .title(getEventTitle(eventType))
            .description(description)
            .performedBy(performedBy)
            .build();
            
        order.getTimeline().add(timelineEvent);
    }
    
    private boolean isCriticalStatus(Order.OrderStatus status) {
        return status == Order.OrderStatus.CONFIRMED ||
               status == Order.OrderStatus.SHIPPED ||
               status == Order.OrderStatus.DELIVERED ||
               status == Order.OrderStatus.CANCELLED;
    }
    
    /**
     * Get event title for timeline events
     */
    private String getEventTitle(OrderTimeline.EventType eventType) {
        return switch (eventType) {
            case PAYMENT_COMPLETED -> "Payment Completed";
            case PAYMENT_FAILED -> "Payment Failed";
            case REFUND_INITIATED -> "Refund Initiated";
            case ORDER_CONFIRMED -> "Order Confirmed";
            case ORDER_PROCESSING -> "Order Processing";
            case ORDER_SHIPPED -> "Order Shipped";
            case ORDER_DELIVERED -> "Order Delivered";
            case ORDER_CANCELLED -> "Order Cancelled";
            default -> "Order Updated";
        };
    }
}