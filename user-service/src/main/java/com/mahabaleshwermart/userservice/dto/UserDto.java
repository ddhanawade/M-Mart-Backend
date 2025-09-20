package com.mahabaleshwermart.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    
    private String id;
    private String email;
    private String name;
    private String phone;
    private String avatar;
    private boolean isVerified;
    private LocalDateTime createdAt;
    private List<AddressDto> addresses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AddressDto {
        private String id;
        private String type;
        private String name;
        private String street;
        private String city;
        private String state;
        private String pincode;
        private String landmark;
        private boolean isDefault;
    }
} 