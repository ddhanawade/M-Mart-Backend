package com.mahabaleshwermart.userservice.repository;

import com.mahabaleshwermart.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if phone exists
     */
    boolean existsByPhone(String phone);
    
    /**
     * Find user by email and active status
     */
    Optional<User> findByEmailAndIsActive(String email, boolean isActive);
    
    /**
     * Find user with addresses
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.addresses WHERE u.id = :userId")
    Optional<User> findByIdWithAddresses(@Param("userId") String userId);
    
    /**
     * Find user with addresses by email
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.addresses WHERE u.email = :email")
    Optional<User> findByEmailWithAddresses(@Param("email") String email);
} 