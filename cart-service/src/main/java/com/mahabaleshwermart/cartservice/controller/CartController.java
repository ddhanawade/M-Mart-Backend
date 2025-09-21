package com.mahabaleshwermart.cartservice.controller;

import com.mahabaleshwermart.cartservice.dto.*;
import com.mahabaleshwermart.cartservice.service.CartService;
import com.mahabaleshwermart.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Cart Controller
 * Handles shopping cart operations for both guest and registered users
 */
@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Shopping cart management endpoints")
public class CartController {
    
    private final CartService cartService;
    
    @Value("${app.security.allow-session-id-query-param:false}")
    private boolean allowSessionIdQueryParam;
    
    @Value("${app.security.session-id-query-secret:}")
    private String sessionIdQuerySecret;
    
    @Value("${app.security.session-id-query-ttl-seconds:300}")
    private long sessionIdQueryTtlSeconds;
    
    /**
     * Get cart summary for authenticated user or guest session
     */
    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieve cart items for authenticated user or guest session")
    public ResponseEntity<ApiResponse<CartSummaryDto>> getUserCart(
            Authentication authentication,
            HttpSession session,
            HttpServletRequest httpRequest,
            @RequestParam(value = "userId", required = false) String userIdParam,
            @RequestParam(value = "sessionId", required = false) String sessionIdQuery,
            @RequestParam(value = "ts", required = false) Long timestamp,
            @RequestParam(value = "sig", required = false) String signature) {
        String headerUserId = httpRequest.getHeader("X-User-Id");
        String userId = headerUserId != null && !headerUserId.isBlank()
                ? headerUserId
                : (userIdParam != null && !userIdParam.isBlank()
                    ? userIdParam
                    : (authentication != null ? authentication.getName() : null));
        String headerSessionId = httpRequest.getHeader("X-Guest-Session");
        
        // Optionally allow secure sessionId via query params (HMAC + TTL)
        if (headerSessionId == null && userId == null && allowSessionIdQueryParam && sessionIdQuery != null && timestamp != null && signature != null) {
            if (isValidSignedSessionId(sessionIdQuery, timestamp, signature)) {
                headerSessionId = sessionIdQuery;
            }
        }
        CartSummaryDto cart;
        if (userId != null) {
            log.info("Get cart request for user: {}", userId);
            cart = cartService.getUserCart(userId);
            return ResponseEntity.ok(ApiResponse.success(cart, "Cart retrieved successfully"));
        } else {
            String sessionId = (headerSessionId != null && !headerSessionId.isBlank()) ? headerSessionId : session.getId();
            log.info("Get cart request for guest session: {}", sessionId);
            cart = cartService.getGuestCart(sessionId);
            return ResponseEntity.ok(ApiResponse.success(cart, "Guest cart retrieved successfully"));
        }
    }
    
    /**
     * Get cart summary for specific user (by userId)
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user cart by ID", description = "Retrieve cart items for specific user ID")
    public ResponseEntity<ApiResponse<CartSummaryDto>> getUserCartById(@PathVariable String userId) {
        log.info("Get cart request for user: {}", userId);
        CartSummaryDto cart = cartService.getUserCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart retrieved successfully"));
    }

    private boolean isValidSignedSessionId(String sessionId, long timestamp, String signature) {
        try {
            if (sessionIdQuerySecret == null || sessionIdQuerySecret.isBlank()) {
                log.warn("SessionId query secret not configured; rejecting signed sessionId");
                return false;
            }
            long now = System.currentTimeMillis() / 1000L;
            if (Math.abs(now - timestamp) > sessionIdQueryTtlSeconds) {
                log.warn("Signed sessionId expired or too far in future");
                return false;
            }
            String data = sessionId + ":" + timestamp;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(sessionIdQuerySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String expected = bytesToHex(raw);
            boolean match = constantTimeEquals(expected, signature);
            if (!match) {
                log.warn("Invalid signature for signed sessionId");
            }
            return match;
        } catch (Exception e) {
            log.error("Error validating signed sessionId", e);
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
    
    /**
     * Get cart summary for guest session
     */
    @GetMapping("/guest")
    @Operation(summary = "Get guest cart", description = "Retrieve cart items for guest session")
    public ResponseEntity<ApiResponse<CartSummaryDto>> getGuestCart(HttpSession session, HttpServletRequest httpRequest) {
        String headerSessionId = httpRequest.getHeader("X-Guest-Session");
        String sessionId = (headerSessionId != null && !headerSessionId.isBlank()) ? headerSessionId : session.getId();
        log.info("Get guest cart request for session: {}", sessionId);
        
        CartSummaryDto cart = cartService.getGuestCart(sessionId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Guest cart retrieved successfully"));
    }
    
    /**
     * Add item to cart
     */
    @PostMapping("/add")
    @Operation(summary = "Add item to cart", description = "Add product to cart for user or guest")
    public ResponseEntity<ApiResponse<CartItemDto>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication,
            HttpSession session,
            HttpServletRequest httpRequest) {
        String headerSessionId = httpRequest.getHeader("X-Guest-Session");
        String headerUserId = httpRequest.getHeader("X-User-Id");
        
        // Priority: 1. Authentication, 2. UserId from request body, 3. Guest session
        String userId = headerUserId != null && !headerUserId.isBlank()
                ? headerUserId
                : (authentication != null ? authentication.getName() : request.getUserId());
        
        CartItemDto cartItem;
        if (userId != null && !userId.trim().isEmpty()) {
            log.info("Adding product {} to user {} cart", request.getProductId(), userId);
            cartItem = cartService.addToUserCart(userId, request.getProductId(), request.getQuantity());
        } else {
            String sessionIdBody = request.getSessionId();
            String sessionId = (sessionIdBody != null && !sessionIdBody.isBlank()) ? sessionIdBody
                    : (headerSessionId != null && !headerSessionId.isBlank()) ? headerSessionId
                    : session.getId();
            log.info("Adding product {} to guest session {} cart", request.getProductId(), sessionId);
            cartItem = cartService.addToGuestCart(sessionId, request.getProductId(), request.getQuantity());
        }
        
        return ResponseEntity.ok(ApiResponse.success(cartItem, "Item added to cart successfully"));
    }
    
    /**
     * Update cart item quantity
     */
    @PutMapping("/items/{itemId}/quantity")
    @Operation(summary = "Update cart item quantity", description = "Update quantity of a specific cart item")
    public ResponseEntity<ApiResponse<CartItemDto>> updateCartItemQuantity(
            @PathVariable String itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        
        log.info("Updating cart item {} quantity to {}", itemId, request.getQuantity());
        
        CartItemDto cartItem = cartService.updateCartItemQuantity(itemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(cartItem, "Cart item quantity updated successfully"));
    }
    
    /**
     * Remove item from cart
     */
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove cart item", description = "Remove specific item from cart")
    public ResponseEntity<ApiResponse<String>> removeCartItem(@PathVariable String itemId) {
        log.info("Removing cart item: {}", itemId);
        
        cartService.removeCartItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully"));
    }
    
    /**
     * Clear entire cart
     */
    @DeleteMapping("/clear")
    @Operation(summary = "Clear cart", description = "Remove all items from cart")
    public ResponseEntity<ApiResponse<String>> clearCart(
            Authentication authentication,
            HttpSession session,
            HttpServletRequest httpRequest,
            @RequestParam(value = "userId", required = false) String userIdParam) {
        String headerSessionId = httpRequest.getHeader("X-Guest-Session");
        String headerUserId = httpRequest.getHeader("X-User-Id");
        
        String userId = headerUserId != null && !headerUserId.isBlank()
                ? headerUserId
                : (userIdParam != null && !userIdParam.isBlank()
                    ? userIdParam
                    : (authentication != null ? authentication.getName() : null));
        
        if (userId != null) {
            log.info("Clearing cart for user: {}", userId);
            cartService.clearUserCart(userId);
        } else {
            String sessionId = (headerSessionId != null && !headerSessionId.isBlank()) ? headerSessionId : session.getId();
            log.info("Clearing cart for session: {}", sessionId);
            cartService.clearGuestCart(sessionId);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }
    
    /**
     * Get cart item count
     */
    @GetMapping("/count")
    @Operation(summary = "Get cart item count", description = "Get total number of items in cart")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(
            Authentication authentication,
            HttpSession session,
            HttpServletRequest httpRequest) {
        String headerSessionId = httpRequest.getHeader("X-Guest-Session");
        String headerUserId = httpRequest.getHeader("X-User-Id");
        
        String userId = headerUserId != null && !headerUserId.isBlank()
                ? headerUserId
                : (authentication != null ? authentication.getName() : null);
        
        int count;
        if (userId != null) {
            count = cartService.getUserCartItemCount(userId);
        } else {
            String sessionId = (headerSessionId != null && !headerSessionId.isBlank()) ? headerSessionId : session.getId();
            count = cartService.getGuestCartItemCount(sessionId);
        }
        
        return ResponseEntity.ok(ApiResponse.success(count, "Cart item count retrieved successfully"));
    }
    
    /**
     * Transfer guest cart to user account (on login)
     */
    @PostMapping("/transfer")
    @Operation(summary = "Transfer guest cart", description = "Transfer guest cart to user account on login")
    public ResponseEntity<ApiResponse<CartSummaryDto>> transferGuestCart(
            @Valid @RequestBody MoveCartRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String headerUserId = httpRequest.getHeader("X-User-Id");
        String userId = headerUserId != null && !headerUserId.isBlank()
                ? headerUserId
                : (authentication != null ? authentication.getName() : request.getUserId());

        log.info("Transferring cart from session {} to user {}", request.getSessionId(), userId);

        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            log.warn("Transfer request missing sessionId");
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("Session ID is required"));
        }
        if (userId == null || userId.isBlank()) {
            log.warn("Transfer request missing userId");
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("User ID is required"));
        }
        
        try {
            CartSummaryDto cart = cartService.transferGuestCartToUser(
                request.getSessionId(), userId, request.isMergeWithExisting());
            return ResponseEntity.ok(ApiResponse.success(cart, "Cart transferred successfully"));
        } catch (Exception ex) {
            log.error("Cart transfer failed for user {} and session {}: {}", userId, request.getSessionId(), ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(ApiResponse.internalServerError("Failed to transfer cart"));
        }
    }
    
    /**
     * Validate and update cart
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate cart", description = "Validate cart items for availability and price changes")
    public ResponseEntity<ApiResponse<CartSummaryDto>> validateCart(
            Authentication authentication,
            HttpSession session,
            HttpServletRequest httpRequest,
            @RequestParam(value = "userId", required = false) String userIdParam) {
        String headerSessionId = httpRequest.getHeader("X-Guest-Session");
        String headerUserId = httpRequest.getHeader("X-User-Id");
        
        String userId = headerUserId != null && !headerUserId.isBlank()
                ? headerUserId
                : (userIdParam != null && !userIdParam.isBlank()
                    ? userIdParam
                    : (authentication != null ? authentication.getName() : null));
        String sessionId = userId == null ? ((headerSessionId != null && !headerSessionId.isBlank()) ? headerSessionId : session.getId()) : null;
        
        log.info("Validating cart for user: {} or session: {}", userId, sessionId);
        
        CartSummaryDto cart = cartService.validateAndUpdateCart(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart validated successfully"));
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if cart service is running")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Cart service is running"));
    }
} 