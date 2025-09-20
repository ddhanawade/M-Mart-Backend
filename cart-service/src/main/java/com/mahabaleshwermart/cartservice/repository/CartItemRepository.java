package com.mahabaleshwermart.cartservice.repository;

import com.mahabaleshwermart.cartservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartItem entity operations
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    
    /**
     * Find cart items by user ID
     */
    List<CartItem> findByUserIdAndActiveTrueOrderByCreatedAtDesc(String userId);
    
    /**
     * Find cart items by session ID (for guest users)
     */
    List<CartItem> findBySessionIdAndActiveTrueOrderByCreatedAtDesc(String sessionId);
    
    /**
     * Find specific cart item by user ID and product ID
     */
    Optional<CartItem> findByUserIdAndProductIdAndActiveTrue(String userId, String productId);
    
    /**
     * Find specific cart item by session ID and product ID
     */
    Optional<CartItem> findBySessionIdAndProductIdAndActiveTrue(String sessionId, String productId);
    
    /**
     * Count active cart items by user ID
     */
    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.userId = :userId AND c.active = true")
    long countActiveItemsByUserId(@Param("userId") String userId);
    
    /**
     * Count active cart items by session ID
     */
    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.sessionId = :sessionId AND c.active = true")
    long countActiveItemsBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * Get total quantity by user ID
     */
    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CartItem c WHERE c.userId = :userId AND c.active = true")
    int getTotalQuantityByUserId(@Param("userId") String userId);
    
    /**
     * Get total quantity by session ID
     */
    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CartItem c WHERE c.sessionId = :sessionId AND c.active = true")
    int getTotalQuantityBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * Get total amount by user ID
     */
    @Query("SELECT COALESCE(SUM(c.totalPrice), 0) FROM CartItem c WHERE c.userId = :userId AND c.active = true")
    java.math.BigDecimal getTotalAmountByUserId(@Param("userId") String userId);
    
    /**
     * Get total amount by session ID
     */
    @Query("SELECT COALESCE(SUM(c.totalPrice), 0) FROM CartItem c WHERE c.sessionId = :sessionId AND c.active = true")
    java.math.BigDecimal getTotalAmountBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * Find cart items by product ID (for stock updates)
     */
    List<CartItem> findByProductIdAndActiveTrue(String productId);
    
    /**
     * Delete all cart items by user ID
     */
    @Modifying
    @Query("UPDATE CartItem c SET c.active = false WHERE c.userId = :userId")
    void clearCartByUserId(@Param("userId") String userId);
    
    /**
     * Delete all cart items by session ID
     */
    @Modifying
    @Query("UPDATE CartItem c SET c.active = false WHERE c.sessionId = :sessionId")
    void clearCartBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * Transfer cart items from session to user (for login)
     */
    @Modifying
    @Query("UPDATE CartItem c SET c.userId = :userId, c.sessionId = null WHERE c.sessionId = :sessionId AND c.active = true")
    void transferCartFromSessionToUser(@Param("sessionId") String sessionId, @Param("userId") String userId);
    
    /**
     * Delete expired cart items (for guest sessions)
     */
    @Modifying
    @Query("UPDATE CartItem c SET c.active = false WHERE c.sessionId IS NOT NULL AND c.createdAt < :expiredBefore")
    void deleteExpiredGuestCartItems(@Param("expiredBefore") LocalDateTime expiredBefore);
    
    /**
     * Find cart items by multiple product IDs
     */
    @Query("SELECT c FROM CartItem c WHERE c.productId IN :productIds AND c.active = true")
    List<CartItem> findByProductIds(@Param("productIds") List<String> productIds);
    
    /**
     * Update product availability in cart
     */
    @Modifying
    @Query("UPDATE CartItem c SET c.available = :available WHERE c.productId = :productId")
    void updateProductAvailability(@Param("productId") String productId, @Param("available") boolean available);
    
    /**
     * Update product price in cart
     */
    @Modifying
    @Query("UPDATE CartItem c SET c.productPrice = :price, c.totalPrice = c.quantity * :price WHERE c.productId = :productId AND c.active = true")
    void updateProductPrice(@Param("productId") String productId, @Param("price") java.math.BigDecimal price);
    
    /**
     * Find cart items that need price/availability update
     */
    @Query("SELECT c FROM CartItem c WHERE c.active = true AND c.updatedAt < :lastUpdateBefore")
    List<CartItem> findItemsNeedingUpdate(@Param("lastUpdateBefore") LocalDateTime lastUpdateBefore);
} 