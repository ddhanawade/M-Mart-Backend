package com.mahabaleshwermart.orderservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embeddable OrderAddress entity for delivery address information
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAddress {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type")
    private AddressType addressType;
    
    @Column(name = "address_name", nullable = false)
    private String addressName;
    
    @Column(name = "street", nullable = false)
    private String street;
    
    @Column(name = "city", nullable = false)
    private String city;
    
    @Column(name = "state", nullable = false)
    private String state;
    
    @Column(name = "pincode", nullable = false)
    private String pincode;
    
    @Column(name = "landmark")
    private String landmark;
    
    @Column(name = "contact_name")
    private String contactName;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    @Column(name = "delivery_instructions", length = 500)
    private String deliveryInstructions;
    
    // Helper methods
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        
        if (addressName != null) {
            address.append(addressName).append(", ");
        }
        
        address.append(street).append(", ");
        
        if (landmark != null && !landmark.trim().isEmpty()) {
            address.append("Near ").append(landmark).append(", ");
        }
        
        address.append(city).append(", ")
               .append(state).append(" - ")
               .append(pincode);
        
        return address.toString();
    }
    
    public enum AddressType {
        HOME,
        WORK,
        OTHER
    }
} 