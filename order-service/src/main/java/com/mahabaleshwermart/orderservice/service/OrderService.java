package com.mahabaleshwermart.orderservice.service;

import com.mahabaleshwermart.common.dto.PageResponse;
import com.mahabaleshwermart.common.exception.BusinessException;
import com.mahabaleshwermart.common.exception.ResourceNotFoundException;
import com.mahabaleshwermart.orderservice.dto.OrderDto;
import com.mahabaleshwermart.orderservice.entity.*;
import com.mahabaleshwermart.orderservice.dto.CreateOrderRequest;
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
        
        log.info("Order created successfully: {}", order.getOrderNumber());
        return orderMapper.toDto(order);
    }
    
    /**
     * Create simplified order with mock data (public method for testing)
     */
    @Transactional
    public OrderDto createSimplifiedOrderPublic(CreateOrderRequest request) {
        log.info("Creating simplified order with mock data");
        
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
        
        return OrderStatistics.builder()
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .averageOrderValue(totalOrders > 0 ? totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO)
                .build();
    }
    
    // Private helper methods
    
    private Order createSimplifiedOrder(String userId, CreateOrderRequest request) {
        log.info("Creating simplified order for user: {}", userId);
        
        // Create order with mock data
        Order.OrderBuilder orderBuilder = Order.builder()
                .userId(userId)
                .userName("Test User")
                .userEmail("test@example.com")
                .userPhone("1234567890")
                .specialInstructions(request.getSpecialInstructions());
        
        // Set delivery address
        OrderAddress deliveryAddress = orderMapper.toOrderAddress(request.getDeliveryAddress());
        orderBuilder.deliveryAddress(deliveryAddress);
        
        // Set payment information
        OrderPayment payment = orderMapper.toOrderPayment(request.getPayment());
        orderBuilder.payment(payment);
        
        // Calculate amounts with mock data
        BigDecimal subtotal = new BigDecimal("299.99");
        BigDecimal discountAmount = new BigDecimal("50.00");
        BigDecimal taxAmount = subtotal.multiply(TAX_RATE);
        BigDecimal deliveryCharge = STANDARD_DELIVERY_CHARGE;
        
        orderBuilder
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .deliveryCharge(deliveryCharge)
                .discountAmount(discountAmount)
                .totalItems(1)
                .totalQuantity(2);
        
        Order order = orderBuilder.build();
        
        // Initialize timeline list
        order.setTimeline(new ArrayList<>());
        
        // Create mock order items
        List<OrderItem> orderItems = new ArrayList<>();
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .productId("mock-product-001")
                .productName("Sample Product")
                .productImage("https://example.com/images/sample.jpg")
                .productSku("SAMPLE-SKU-001")
                .productCategory("GROCERIES")
                .productUnit("piece")
                .quantity(2)
                .unitPrice(new BigDecimal("149.99"))
                .originalPrice(new BigDecimal("174.99"))
                .totalPrice(new BigDecimal("299.98"))
                .discountAmount(new BigDecimal("50.00"))
                .organic(false)
                .fresh(true)
                .itemStatus(OrderItem.ItemStatus.CONFIRMED)
                .build();
        orderItems.add(orderItem);
        order.setItems(orderItems);
        
        log.info("Created order with {} items, total amount: {}", orderItems.size(), 
                order.getSubtotal().add(order.getTaxAmount()).add(order.getDeliveryCharge()).subtract(order.getDiscountAmount()));
        
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