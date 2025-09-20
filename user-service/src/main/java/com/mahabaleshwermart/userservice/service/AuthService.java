package com.mahabaleshwermart.userservice.service;

import com.mahabaleshwermart.common.exception.BusinessException;
import com.mahabaleshwermart.common.exception.ResourceNotFoundException;
import com.mahabaleshwermart.common.exception.UnauthorizedException;
import com.mahabaleshwermart.userservice.dto.LoginRequest;
import com.mahabaleshwermart.userservice.dto.RegisterRequest;
import com.mahabaleshwermart.userservice.dto.AuthResponse;
import com.mahabaleshwermart.userservice.dto.UserDto;
import com.mahabaleshwermart.userservice.entity.User;
import com.mahabaleshwermart.userservice.mapper.UserMapper;
import com.mahabaleshwermart.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service
 * Handles user login, registration, and token management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    
    /**
     * Authenticate user and generate tokens
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            User user = (User) authentication.getPrincipal();
            
            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            UserDto userDto = userMapper.toDto(user);
            
            log.info("User {} logged in successfully", user.getEmail());
            
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getAccessTokenExpiration())
                    .user(userDto)
                    .build();
                    
        } catch (AuthenticationException e) {
            log.error("Authentication failed for email: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }
    }
    
    /**
     * Register new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already registered");
        }
        
        // Check if phone already exists (if provided)
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException("Phone number is already registered");
            }
        }
        
        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .isVerified(true) // For demo purposes, auto-verify
                .isActive(true)
                .build();
        
        user = userRepository.save(user);
        
        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        UserDto userDto = userMapper.toDto(user);
        
        log.info("User {} registered successfully", user.getEmail());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .user(userDto)
                .build();
    }
    
    /**
     * Refresh access token
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Token refresh attempt");
        
        // Validate refresh token
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        
        // Extract user email from token
        String email = jwtService.extractEmailFromRefreshToken(refreshToken);
        
        // Find user
        User user = userRepository.findByEmailAndIsActive(email, true)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);
        
        UserDto userDto = userMapper.toDto(user);
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Keep the same refresh token
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .user(userDto)
                .build();
    }
    
    /**
     * Logout user (invalidate tokens - for Redis implementation)
     */
    @Transactional
    public void logout(String accessToken) {
        log.info("User logout");
        // In a production system, you would add the token to a blacklist in Redis
        // For now, we'll just log the logout action
    }
    
    /**
     * Get current authenticated user
     */
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmailWithAddresses(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return userMapper.toDto(user);
    }
} 