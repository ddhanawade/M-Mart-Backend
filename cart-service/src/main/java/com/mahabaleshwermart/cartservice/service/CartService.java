package com.mahabaleshwermart.cartservice.service;

import com.mahabaleshwermart.cartservice.dto.CartItemDto;
import com.mahabaleshwermart.cartservice.dto.CartSummaryDto;
import com.mahabaleshwermart.cartservice.entity.CartItem;
import com.mahabaleshwermart.cartservice.external.ProductServiceClient;
import com.mahabaleshwermart.cartservice.mapper.CartMapper;
import com.mahabaleshwermart.cartservice.repository.CartItemRepository;
import com.mahabaleshwermart.common.exception.BusinessException;
import com.mahabaleshwermart.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Cart Service
 * Handles shopping cart operations for both guest and registered users
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final ProductServiceClient productServiceClient;
    
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = BigDecimal.valueOf(500);
    private static final BigDecimal STANDARD_DELIVERY_CHARGE = BigDecimal.valueOf(50);
    
    /**
     * Get cart items for user
     */
    @Cacheable(value = "user-cart", key = "#userId")
    @Transactional(readOnly = true)
    public CartSummaryDto getUserCart(String userId) {
        log.info("Fetching cart for user: {}", userId);
        
        List<CartItem> cartItems = cartItemRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId);
        return buildCartSummary(cartItems);
    }
    
    /**
     * Get cart items for guest session
     */
    @Transactional(readOnly = true)
    public CartSummaryDto getGuestCart(String sessionId) {
        log.info("Fetching cart for session: {}", sessionId);
        
        List<CartItem> cartItems = cartItemRepository.findBySessionIdAndActiveTrueOrderByCreatedAtDesc(sessionId);
        return buildCartSummary(cartItems);
    }
    
    /**
     * Add item to cart (user)
     */
    @CacheEvict(value = "user-cart", key = "#userId")
    @Transactional
    public CartItemDto addToUserCart(String userId, String productId, int quantity) {
        log.info("Adding product {} to user {} cart with quantity {}", productId, userId, quantity);
        
        // Fetch product details
        var productResponse = productServiceClient.getProductById(productId);
        if (productResponse == null || !productResponse.isSuccess() || productResponse.getData() == null) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        var productDto = productResponse.getData();
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductIdAndActiveTrue(userId, productId);
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update existing item quantity
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setSelectedQuantity(cartItem.getQuantity());
        } else {
            // Create new cart item
            cartItem = CartItem.builder()
                    .userId(userId)
                    .productId(productId)
                    .productName(productDto.getName())
                    .productImage(productDto.getImage())
                    .productPrice(productDto.getPrice())
                    .originalPrice(productDto.getOriginalPrice())
                    .productUnit(productDto.getUnit())
                    .quantity(quantity)
                    .selectedQuantity(quantity)
                    .available(productDto.isInStock())
                    .productCategory(productDto.getCategory())
                    .productSku(productDto.getSku())
                    .organic(productDto.isOrganic())
                    .fresh(productDto.isFresh())
                    .addedAt(LocalDateTime.now())
                    .build();
        }
        
        cartItem = cartItemRepository.save(cartItem);
        log.info("Product added to cart successfully: {}", cartItem.getId());
        
        return cartMapper.toDto(cartItem);
    }
    
    /**
     * Add item to guest cart
     */
    @Transactional
    public CartItemDto addToGuestCart(String sessionId, String productId, int quantity) {
        log.info("Adding product {} to session {} cart with quantity {}", productId, sessionId, quantity);
        
        // Fetch product details
        var productResponse = productServiceClient.getProductById(productId);
        if (productResponse == null || !productResponse.isSuccess() || productResponse.getData() == null) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        var productDto = productResponse.getData();
        
        // Check if item already exists in session cart
        Optional<CartItem> existingItem = cartItemRepository.findBySessionIdAndProductIdAndActiveTrue(sessionId, productId);
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update existing item quantity
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setSelectedQuantity(cartItem.getQuantity());
        } else {
            // Create new cart item
            cartItem = CartItem.builder()
                    .sessionId(sessionId)
                    .productId(productId)
                    .productName(productDto.getName())
                    .productImage(productDto.getImage())
                    .productPrice(productDto.getPrice())
                    .originalPrice(productDto.getOriginalPrice())
                    .productUnit(productDto.getUnit())
                    .quantity(quantity)
                    .selectedQuantity(quantity)
                    .available(productDto.isInStock())
                    .productCategory(productDto.getCategory())
                    .productSku(productDto.getSku())
                    .organic(productDto.isOrganic())
                    .fresh(productDto.isFresh())
                    .addedAt(LocalDateTime.now())
                    .build();
        }
        
        cartItem = cartItemRepository.save(cartItem);
        log.info("Product added to guest cart successfully: {}", cartItem.getId());
        
        return cartMapper.toDto(cartItem);
    }
    
    /**
     * Update cart item quantity
     */
    @CacheEvict(value = "user-cart", allEntries = true)
    @Transactional
    public CartItemDto updateCartItemQuantity(String cartItemId, int quantity) {
        log.info("Updating cart item {} quantity to {}", cartItemId, quantity);
        
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be greater than 0");
        }
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", cartItemId));
        
        cartItem.setQuantity(quantity);
        cartItem.setSelectedQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);
        
        log.info("Cart item quantity updated successfully: {}", cartItemId);
        return cartMapper.toDto(cartItem);
    }
    
    /**
     * Remove item from cart
     */
    @CacheEvict(value = "user-cart", allEntries = true)
    @Transactional
    public void removeCartItem(String cartItemId) {
        log.info("Removing cart item: {}", cartItemId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", cartItemId));
        
        cartItem.setActive(false);
        cartItemRepository.save(cartItem);
        
        log.info("Cart item removed successfully: {}", cartItemId);
    }
    
    /**
     * Clear entire cart for user
     */
    @CacheEvict(value = "user-cart", key = "#userId")
    @Transactional
    public void clearUserCart(String userId) {
        log.info("Clearing cart for user: {}", userId);
        
        cartItemRepository.clearCartByUserId(userId);
        
        log.info("User cart cleared successfully: {}", userId);
    }
    
    /**
     * Clear entire cart for guest session
     */
    @Transactional
    public void clearGuestCart(String sessionId) {
        log.info("Clearing cart for session: {}", sessionId);
        
        cartItemRepository.clearCartBySessionId(sessionId);
        
        log.info("Guest cart cleared successfully: {}", sessionId);
    }
    
    /**
     * Transfer guest cart to user account (on login)
     */
    @CacheEvict(value = "user-cart", key = "#userId")
    @Transactional
    public CartSummaryDto transferGuestCartToUser(String sessionId, String userId, boolean mergeWithExisting) {
        log.info("Transferring cart from session {} to user {}", sessionId, userId);
        
        if (mergeWithExisting) {
            // Merge guest cart with existing user cart
            List<CartItem> guestItems = cartItemRepository.findBySessionIdAndActiveTrueOrderByCreatedAtDesc(sessionId);
            
            for (CartItem guestItem : guestItems) {
                Optional<CartItem> existingUserItem = cartItemRepository
                        .findByUserIdAndProductIdAndActiveTrue(userId, guestItem.getProductId());
                
                if (existingUserItem.isPresent()) {
                    // Merge quantities
                    CartItem userItem = existingUserItem.get();
                    userItem.setQuantity(userItem.getQuantity() + guestItem.getQuantity());
                    userItem.setSelectedQuantity(userItem.getQuantity());
                    cartItemRepository.save(userItem);
                    
                    // Remove guest item
                    guestItem.setActive(false);
                    cartItemRepository.save(guestItem);
                } else {
                    // Transfer guest item to user
                    guestItem.setUserId(userId);
                    guestItem.setSessionId(null);
                    cartItemRepository.save(guestItem);
                }
            }
        } else {
            // Simple transfer without merging
            cartItemRepository.transferCartFromSessionToUser(sessionId, userId);
        }
        
        log.info("Cart transfer completed successfully");
        return getUserCart(userId);
    }
    
    /**
     * Get cart item count for user
     */
    @Transactional(readOnly = true)
    public int getUserCartItemCount(String userId) {
        return (int) cartItemRepository.countActiveItemsByUserId(userId);
    }
    
    /**
     * Get cart item count for guest session
     */
    @Transactional(readOnly = true)
    public int getGuestCartItemCount(String sessionId) {
        return (int) cartItemRepository.countActiveItemsBySessionId(sessionId);
    }
    
    /**
     * Validate cart items (check availability and prices)
     */
    @Transactional
    public CartSummaryDto validateAndUpdateCart(String userId, String sessionId) {
        log.info("Validating cart for user: {} or session: {}", userId, sessionId);
        
        List<CartItem> cartItems;
        if (userId != null) {
            cartItems = cartItemRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId);
        } else {
            cartItems = cartItemRepository.findBySessionIdAndActiveTrueOrderByCreatedAtDesc(sessionId);
        }
        
        boolean cartUpdated = false;
        
        for (CartItem item : cartItems) {
            try {
                var productResponse = productServiceClient.getProductById(item.getProductId());
                if (productResponse == null || !productResponse.isSuccess() || productResponse.getData() == null) {
                    throw new RuntimeException("Product not found");
                }
                var productDto = productResponse.getData();
                
                // Update product information if changed
                if (!item.getProductPrice().equals(productDto.getPrice()) ||
                    item.isAvailable() != productDto.isInStock()) {
                    
                    item.setProductPrice(productDto.getPrice());
                    item.setOriginalPrice(productDto.getOriginalPrice());
                    item.setAvailable(productDto.isInStock());
                    item.setProductName(productDto.getName());
                    item.setProductImage(productDto.getImage());
                    
                    cartItemRepository.save(item);
                    cartUpdated = true;
                }
            } catch (Exception e) {
                log.warn("Product {} not found, marking as unavailable", item.getProductId());
                item.setAvailable(false);
                cartItemRepository.save(item);
                cartUpdated = true;
            }
        }
        
        if (cartUpdated) {
            log.info("Cart validation completed with updates");
            // Clear cache to force refresh
            if (userId != null) {
                // Cache will be refreshed on next call
            }
        }
        
        return buildCartSummary(cartItems);
    }
    
    /**
     * Clean up expired guest cart items
     */
    @Transactional
    public void cleanupExpiredGuestCarts() {
        log.info("Cleaning up expired guest cart items");
        
        LocalDateTime expiredBefore = LocalDateTime.now().minusDays(7); // 7 days old
        cartItemRepository.deleteExpiredGuestCartItems(expiredBefore);
        
        log.info("Expired guest cart items cleanup completed");
    }
    
    // Private helper methods
    
    private CartSummaryDto buildCartSummary(List<CartItem> cartItems) {
        List<CartItemDto> itemDtos = cartMapper.toDtoList(cartItems);
        
        if (itemDtos.isEmpty()) {
            return CartSummaryDto.builder()
                    .items(itemDtos)
                    .totalItems(0)
                    .totalQuantity(0)
                    .subtotal(BigDecimal.ZERO)
                    .totalSavings(BigDecimal.ZERO)
                    .deliveryCharge(BigDecimal.ZERO)
                    .totalAmount(BigDecimal.ZERO)
                    .hasOutOfStockItems(false)
                    .hasUnavailableItems(false)
                    .lastUpdated(LocalDateTime.now())
                    .build();
        }
        
        int totalItems = itemDtos.size();
        int totalQuantity = itemDtos.stream().mapToInt(CartItemDto::getQuantity).sum();
        
        BigDecimal subtotal = itemDtos.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalSavings = itemDtos.stream()
                .map(CartItemDto::getSavings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal deliveryCharge = subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0 
                ? BigDecimal.ZERO : STANDARD_DELIVERY_CHARGE;
        
        BigDecimal totalAmount = subtotal.add(deliveryCharge);
        
        boolean hasOutOfStockItems = itemDtos.stream().anyMatch(item -> !item.isAvailable());
        boolean hasUnavailableItems = hasOutOfStockItems; // Same for now
        
        LocalDateTime lastUpdated = cartItems.stream()
                .map(CartItem::getUpdatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        
        return CartSummaryDto.builder()
                .items(itemDtos)
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .subtotal(subtotal)
                .totalSavings(totalSavings)
                .deliveryCharge(deliveryCharge)
                .totalAmount(totalAmount)
                .hasOutOfStockItems(hasOutOfStockItems)
                .hasUnavailableItems(hasUnavailableItems)
                .lastUpdated(lastUpdated)
                .build();
    }
} 