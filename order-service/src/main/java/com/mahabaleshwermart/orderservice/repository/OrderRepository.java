package com.mahabaleshwermart.orderservice.repository;

import com.mahabaleshwermart.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity operations
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Find orders by user ID
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    /**
     * Find orders by user ID and status
     */
    Page<Order> findByUserIdAndOrderStatusOrderByCreatedAtDesc(
        String userId, Order.OrderStatus orderStatus, Pageable pageable);
    
    /**
     * Find orders by status
     */
    Page<Order> findByOrderStatusOrderByCreatedAtDesc(Order.OrderStatus orderStatus, Pageable pageable);
    
    /**
     * Find orders by payment status
     */
    Page<Order> findByPaymentStatusOrderByCreatedAtDesc(Order.PaymentStatus paymentStatus, Pageable pageable);
    
    /**
     * Find orders created between dates
     */
    Page<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(
        LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find orders by user and date range
     */
    Page<Order> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find recent orders by user
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUser(@Param("userId") String userId, Pageable pageable);
    
    /**
     * Get order count by user
     */
    long countByUserId(String userId);
    
    /**
     * Get order count by status
     */
    long countByOrderStatus(Order.OrderStatus orderStatus);
    
    /**
     * Get total order value by user
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.userId = :userId AND o.orderStatus != :excludeStatus")
    BigDecimal getTotalOrderValueByUser(@Param("userId") String userId, @Param("excludeStatus") Order.OrderStatus excludeStatus);
    
    /**
     * Find orders requiring action (pending confirmation, processing, etc.)
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus IN ('PENDING', 'CONFIRMED', 'PROCESSING') ORDER BY o.createdAt ASC")
    List<Order> findOrdersRequiringAction();
    
    /**
     * Find orders for delivery today
     */
    @Query("SELECT o FROM Order o WHERE DATE(o.estimatedDelivery) = CURRENT_DATE AND o.orderStatus IN ('SHIPPED', 'OUT_FOR_DELIVERY')")
    List<Order> findOrdersForDeliveryToday();
    
    /**
     * Find overdue orders (past estimated delivery)
     */
    @Query("SELECT o FROM Order o WHERE o.estimatedDelivery < :currentTime AND o.orderStatus NOT IN ('DELIVERED', 'CANCELLED', 'RETURNED')")
    List<Order> findOverdueOrders(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Get orders summary by date range
     */
    @Query("SELECT DATE(o.createdAt) as orderDate, COUNT(o) as orderCount, SUM(o.totalAmount) as totalAmount " +
           "FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(o.createdAt) ORDER BY DATE(o.createdAt)")
    List<Object[]> getOrdersSummaryByDateRange(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get orders summary by status
     */
    @Query("SELECT o.orderStatus, COUNT(o) as orderCount, SUM(o.totalAmount) as totalAmount " +
           "FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY o.orderStatus")
    List<Object[]> getOrdersSummaryByStatus(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find orders with specific items (by product ID)
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.productId = :productId")
    List<Order> findOrdersWithProduct(@Param("productId") String productId);
    
    /**
     * Find high-value orders
     */
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount ORDER BY o.totalAmount DESC")
    List<Order> findHighValueOrders(@Param("minAmount") BigDecimal minAmount);
    
    /**
     * Find orders by tracking number
     */
    Optional<Order> findByTrackingNumber(String trackingNumber);
    
    /**
     * Find orders by invoice number
     */
    Optional<Order> findByInvoiceNumber(String invoiceNumber);
    
    /**
     * Get monthly order statistics
     */
    @Query("SELECT YEAR(o.createdAt) as year, MONTH(o.createdAt) as month, " +
           "COUNT(o) as orderCount, SUM(o.totalAmount) as totalAmount, AVG(o.totalAmount) as averageAmount " +
           "FROM Order o WHERE o.createdAt >= :fromDate " +
           "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
           "ORDER BY YEAR(o.createdAt) DESC, MONTH(o.createdAt) DESC")
    List<Object[]> getMonthlyOrderStatistics(@Param("fromDate") LocalDateTime fromDate);
    
    /**
     * Find orders needing refund processing
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'CANCELLED' AND o.paymentStatus = 'COMPLETED' AND o.refundAmount IS NULL")
    List<Order> findOrdersNeedingRefund();
    
    /**
     * Search orders by order number or user email
     */
    @Query("SELECT o FROM Order o WHERE LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(o.userEmail) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> searchOrders(@Param("search") String search, Pageable pageable);
} 