package com.mahabaleshwermart.orderservice.service;

import com.mahabaleshwermart.common.dto.PageResponse;
import com.mahabaleshwermart.common.exception.BusinessException;
import com.mahabaleshwermart.common.exception.ResourceNotFoundException;
import com.mahabaleshwermart.orderservice.dto.OrderDto;
import com.mahabaleshwermart.orderservice.entity.*;
import com.mahabaleshwermart.orderservice.external.CartServiceClient;
import com.mahabaleshwermart.orderservice.external.CartItemDto;
import com.mahabaleshwermart.orderservice.external.CartSummaryDto;
import com.mahabaleshwermart.orderservice.external.UserServiceClient;
import com.mahabaleshwermart.orderservice.external.UserDto;
import com.mahabaleshwermart.orderservice.dto.CreateOrderRequest;
import com.mahabaleshwermart.orderservice.dto.CreateOrderPaymentRequest;
import com.mahabaleshwermart.orderservice.external.ProductServiceClient;
import com.mahabaleshwermart.orderservice.mapper.OrderMapper;
import com.mahabaleshwermart.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final CartServiceClient cartServiceClient;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.18); // 18% GST
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = BigDecimal.valueOf(500);
    private static final BigDecimal STANDARD_DELIVERY_CHARGE = BigDecimal.valueOf(50);
    
    /**
     * Create order from cart
     */
    @Transactional
    public OrderDto createOrderFromCart(String userId, CreateOrderRequest request) {
        log.info("Creating order from cart for user: {}", userId);
        
        // Get user details
        var userDto = userServiceClient.getUserById(userId);
        if (userDto == null) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        // Get cart items
        var cartSummary = cartServiceClient.getUserCart(userId);
        if (cartSummary == null || cartSummary.items().isEmpty()) {
            throw new BusinessException("Cart is empty. Cannot create order.");
        }
        
        // Validate cart items availability and prices
        cartServiceClient.validateCart(userId);
        
        // Create order
        Order order = createOrderFromCartItems(userDto, cartSummary, request);
        order = orderRepository.save(order);
        
        // Add initial timeline event
        OrderTimeline orderPlacedEvent = OrderTimeline.orderPlaced(order, userId);
        if (order.getTimeline() != null) {
            order.getTimeline().add(orderPlacedEvent);
        }
        
        // Process payment
        if (!"CASH_ON_DELIVERY".equals(request.getPayment().getPaymentMethod())) {
            processPayment(order, request.getPayment());
        }
        
        // Clear cart after successful order creation
        cartServiceClient.clearUserCart(userId);
        
        // Send order confirmation notification
        notificationService.sendOrderConfirmation(order);
        
        log.info("Order created successfully: {}", order.getOrderNumber());
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
        notificationService.sendOrderStatusUpdate(order, oldStatus, newStatus);
        
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
        notificationService.sendOrderCancellation(order, reason);
        
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
        
        return OrderStatistics.builder()
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .averageOrderValue(totalOrders > 0 ? totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO)
                .build();
    }
    
    // Private helper methods
    
    private Order createOrderFromCartItems(UserDto userDto, CartSummaryDto cartSummary, CreateOrderRequest request) {
        Order.OrderBuilder orderBuilder = Order.builder()
                .userId(userDto.id())
                .userName(userDto.name())
                .userEmail(userDto.email())
                .userPhone(userDto.phone())
                .specialInstructions(request.getSpecialInstructions());
        
        // Set delivery address
        OrderAddress deliveryAddress = orderMapper.toOrderAddress(request.getDeliveryAddress());
        orderBuilder.deliveryAddress(deliveryAddress);
        
        // Set payment information
        OrderPayment payment = orderMapper.toOrderPayment(request.getPayment());
        orderBuilder.payment(payment);
        
        // Calculate amounts
        BigDecimal subtotal = cartSummary.subtotal();
        BigDecimal taxAmount = subtotal.multiply(TAX_RATE);
        BigDecimal deliveryCharge = subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0 ? 
                BigDecimal.ZERO : STANDARD_DELIVERY_CHARGE;
        
        orderBuilder
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .deliveryCharge(deliveryCharge)
                .discountAmount(cartSummary.totalSavings())
                .totalItems(cartSummary.totalItems())
                .totalQuantity(cartSummary.totalQuantity());
        
        Order order = orderBuilder.build();
        
        // Add order items
        List<OrderItem> orderItems = cartSummary.items().stream()
                .map(cartItem -> orderMapper.toOrderItem(cartItem, order))
                .toList();
        order.setItems(orderItems);
        
        return order;
    }
    
    private void processPayment(Order order, CreateOrderPaymentRequest paymentRequest) {
        try {
            PaymentResult result = paymentService.processPayment(order, paymentRequest);
            
            if (result.isSuccessful()) {
                order.setPaymentStatus(Order.PaymentStatus.COMPLETED);
                order.getPayment().setPaymentId(result.getPaymentId());
                order.getPayment().setTransactionId(result.getTransactionId());
                order.getPayment().setPaymentDate(LocalDateTime.now());
                order.getPayment().setPaidAmount(order.getTotalAmount());
                
                // Add payment completion timeline event
                OrderTimeline paymentEvent = OrderTimeline.paymentCompleted(order, result.getTransactionId());
                order.getTimeline().add(paymentEvent);
            } else {
                order.setPaymentStatus(Order.PaymentStatus.FAILED);
                order.getPayment().setFailureReason(result.getFailureReason());
                throw new BusinessException("Payment failed: " + result.getFailureReason());
            }
        } catch (Exception e) {
            log.error("Payment processing failed for order: {}", order.getOrderNumber(), e);
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
            order.getPayment().setFailureReason(e.getMessage());
            throw new BusinessException("Payment processing failed: " + e.getMessage());
        }
    }
    
    private void processRefund(Order order, BigDecimal refundAmount, String reason) {
        try {
            RefundResult result = paymentService.processRefund(order, refundAmount, reason);
            
            if (result.isSuccessful()) {
                order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
                order.setRefundAmount(refundAmount);
                order.getPayment().setRefundId(result.getRefundId());
                order.getPayment().setRefundAmount(refundAmount);
                order.getPayment().setRefundDate(LocalDateTime.now());
                order.getPayment().setRefundReason(reason);
            }
        } catch (Exception e) {
            log.error("Refund processing failed for order: {}", order.getOrderNumber(), e);
        }
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
            case CONFIRMED -> OrderTimeline.EventType.ORDER_CONFIRMED;
            case PROCESSING -> OrderTimeline.EventType.ORDER_PROCESSING;
            case PACKED -> OrderTimeline.EventType.ORDER_PACKED;
            case SHIPPED -> OrderTimeline.EventType.ORDER_SHIPPED;
            case OUT_FOR_DELIVERY -> OrderTimeline.EventType.OUT_FOR_DELIVERY;
            case DELIVERED -> OrderTimeline.EventType.ORDER_DELIVERED;
            case CANCELLED -> OrderTimeline.EventType.ORDER_CANCELLED;
            case RETURNED -> OrderTimeline.EventType.ORDER_RETURNED;
            default -> OrderTimeline.EventType.INTERNAL_NOTE;
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
    
    private boolean isCriticalStatus(Order.OrderStatus status) {
        return status == Order.OrderStatus.CONFIRMED ||
               status == Order.OrderStatus.SHIPPED ||
               status == Order.OrderStatus.DELIVERED ||
               status == Order.OrderStatus.CANCELLED;
    }
}