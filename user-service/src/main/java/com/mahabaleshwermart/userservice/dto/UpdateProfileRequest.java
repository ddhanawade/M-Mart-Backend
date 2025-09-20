package com.mahabaleshwermart.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request payload for updating the current user's profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateProfileRequest {

    private String name; // optional

    // Optional Indian phone format (basic). Keep loose to avoid over-restriction.
    @Pattern(regexp = "^[0-9+\\- ()]{7,15}$", message = "Invalid phone format")
    private String phone;

    private List<AddressDto> addresses; // optional; replaces existing addresses if provided

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AddressDto {
        private String id; // optional for update; new addresses may omit id
        private String type; // home | work | other
        private String name;
        private String street;
        private String city;
        private String state;
        private String pincode;
        private String landmark;
        private Boolean isDefault;
    }
}


