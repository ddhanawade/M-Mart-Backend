package com.mahabaleshwermart.userservice.repository;

import com.mahabaleshwermart.userservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Address entity operations
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    
    /**
     * Find all addresses by user ID
     */
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtAsc(String userId);
    
    /**
     * Find user's default address
     */
    Optional<Address> findByUserIdAndIsDefaultTrue(String userId);
    
    /**
     * Find address by ID and user ID
     */
    Optional<Address> findByIdAndUserId(String id, String userId);
    
    /**
     * Count addresses by user ID
     */
    long countByUserId(String userId);
    
    /**
     * Update all addresses of a user to non-default
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void updateAllToNonDefault(@Param("userId") String userId);
    
    /**
     * Check if user has any addresses
     */
    boolean existsByUserId(String userId);
} 