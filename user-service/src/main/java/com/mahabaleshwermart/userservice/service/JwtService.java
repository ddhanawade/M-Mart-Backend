package com.mahabaleshwermart.userservice.service;

import com.mahabaleshwermart.userservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Service for token generation and validation
 * Uses Java 21 features for enhanced security and performance
 */
@Slf4j
@Service
public class JwtService {
    
    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;
    
    @Value("${jwt.access-token-expiration:86400000}") // 24 hours
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration:604800000}") // 7 days
    private long refreshTokenExpiration;
    
    /**
     * Generate access token for user
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("verified", user.isVerified());
        claims.put("type", "access");
        
        return createToken(claims, user.getEmail(), accessTokenExpiration);
    }
    
    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("type", "refresh");
        
        return createToken(claims, user.getEmail(), refreshTokenExpiration);
    }
    
    /**
     * Extract email from access token
     */
    public String extractEmailFromAccessToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract email from refresh token
     */
    public String extractEmailFromRefreshToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract user ID from token
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }
    
    /**
     * Extract user role from token
     */
    public String extractUserRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    /**
     * Validate access token
     */
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = claims.get("type", String.class);
            return "access".equals(tokenType) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Invalid access token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate refresh token
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = claims.get("type", String.class);
            return "refresh".equals(tokenType) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get access token expiration in seconds
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration / 1000; // Convert to seconds
    }
    
    /**
     * Get refresh token expiration in seconds
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration / 1000; // Convert to seconds
    }
    
    // Private helper methods
    
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = now.plusSeconds(expiration / 1000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(expirationTime.toInstant(ZoneOffset.UTC)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 