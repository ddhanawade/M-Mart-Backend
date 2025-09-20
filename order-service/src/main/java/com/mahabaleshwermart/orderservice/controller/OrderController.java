package com.mahabaleshwermart.orderservice.controller;

import com.mahabaleshwermart.common.dto.ApiResponse;
import com.mahabaleshwermart.common.dto.PageResponse;
import com.mahabaleshwermart.orderservice.dto.OrderDto;
import com.mahabaleshwermart.orderservice.dto.CreateOrderRequest;
import com.mahabaleshwermart.orderservice.entity.Order;
import com.mahabaleshwermart.orderservice.service.OrderService;
import com.mahabaleshwermart.orderservice.dto.CreateOrderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.mahabaleshwermart.orderservice.service.OrderStatistics;
import com.mahabaleshwermart.orderservice.service.OrderService;
import com.mahabaleshwermart.orderservice.dto.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Order Controller
 * Handles order processing, tracking, and management operations
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order processing and management endpoints")
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Create order from cart
     */
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create order", description = "Create order from user's cart")
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        
        String userId = authentication.getName();
        log.info("Create order request for user: {}", userId);
        
        OrderDto order = orderService.createOrderFromCart(userId, request);
        
        return ResponseEntity.status(201).body(
            ApiResponse.created(order, "Order created successfully")
        );
    }
    
    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get order by ID", description = "Retrieve order details by order ID")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable String orderId) {
        log.info("Get order by ID: {}", orderId);
        
        OrderDto order = orderService.getOrderById(orderId);
        
        return ResponseEntity.ok(
            ApiResponse.success(order, "Order retrieved successfully")
        );
    }
    
    /**
     * Get order by order number
     */
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by number", description = "Retrieve order details by order number")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderByNumber(@PathVariable String orderNumber) {
        log.info("Get order by number: {}", orderNumber);
        
        OrderDto order = orderService.getOrderByOrderNumber(orderNumber);
        
        return ResponseEntity.ok(
            ApiResponse.success(order, "Order retrieved successfully")
        );
    }
    
    /**
     * Get user orders
     */
    @GetMapping("/my-orders")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get user orders", description = "Retrieve orders for authenticated user")
    public ResponseEntity<ApiResponse<PageResponse<OrderDto>>> getUserOrders(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication) {
        
        String userId = authentication.getName();
        log.info("Get orders for user: {}", userId);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        
        PageResponse<OrderDto> orders = orderService.getUserOrders(userId, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(orders, "Orders retrieved successfully")
        );
    }
    
    /**
     * Get orders by status (Admin only)
     */
    @GetMapping("/status/{status}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get orders by status", description = "Retrieve orders by status (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<OrderDto>>> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Get orders by status: {}", status);
        
        Order.OrderStatus orderStatus;
        try {
            orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.badRequest("Invalid order status: " + status)
            );
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<OrderDto> orders = orderService.getOrdersByStatus(orderStatus, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(orders, "Orders retrieved successfully")
        );
    }
    
    /**
     * Update order status (Admin only)
     */
    @PutMapping("/{orderId}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update order status", description = "Update order status (Admin only)")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody com.mahabaleshwermart.orderservice.dto.UpdateOrderStatusRequest request,
            Authentication authentication) {
        
        String performedBy = authentication.getName();
        log.info("Update order {} status to {} by {}", orderId, request.getOrderStatus(), performedBy);
        
        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(request.getOrderStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.badRequest("Invalid order status: " + request.getOrderStatus())
            );
        }
        
        OrderDto order = orderService.updateOrderStatus(orderId, newStatus, request.getNotes(), performedBy);
        
        return ResponseEntity.ok(
            ApiResponse.success(order, "Order status updated successfully")
        );
    }
    
    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancel order", description = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @PathVariable String orderId,
            @Valid @RequestBody com.mahabaleshwermart.orderservice.dto.CancelOrderRequest request,
            Authentication authentication) {
        
        String performedBy = authentication.getName();
        log.info("Cancel order {} by {} with reason: {}", orderId, performedBy, request.getReason());
        
        OrderDto order = orderService.cancelOrder(orderId, request.getReason(), performedBy);
        
        return ResponseEntity.ok(
            ApiResponse.success(order, "Order cancelled successfully")
        );
    }
    
    /**
     * Track order
     */
    @GetMapping("/track/{orderNumber}")
    @Operation(summary = "Track order", description = "Track order by order number")
    public ResponseEntity<ApiResponse<OrderDto>> trackOrder(@PathVariable String orderNumber) {
        log.info("Track order: {}", orderNumber);
        
        OrderDto order = orderService.trackOrder(orderNumber);
        
        return ResponseEntity.ok(
            ApiResponse.success(order, "Order tracking information retrieved successfully")
        );
    }
    
    /**
     * Search orders (Admin only)
     */
    @GetMapping("/search")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Search orders", description = "Search orders by order number or user email (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<OrderDto>>> searchOrders(
            @Parameter(description = "Search term") @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Search orders with term: {}", q);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<OrderDto> orders = orderService.searchOrders(q, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(orders, "Search results retrieved successfully")
        );
    }
    
    /**
     * Get order statistics
     */
    @GetMapping("/statistics")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get order statistics", description = "Get order statistics for user")
    public ResponseEntity<ApiResponse<com.mahabaleshwermart.orderservice.service.OrderStatistics>> getOrderStatistics(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {
        
        String userId = authentication.getName();
        log.info("Get order statistics for user: {} for last {} days", userId, days);
        
        com.mahabaleshwermart.orderservice.service.OrderStatistics statistics = orderService.getOrderStatistics(userId, days);
        
        return ResponseEntity.ok(
            ApiResponse.success(statistics, "Order statistics retrieved successfully")
        );
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if order service is running")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("Order service is running")
        );
    }
}

// Remove local record DTOs; consolidated in dto package

// Remove local OrderStatistics record; using service.OrderStatistics class