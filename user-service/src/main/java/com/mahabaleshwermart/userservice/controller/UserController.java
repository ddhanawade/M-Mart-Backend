package com.mahabaleshwermart.userservice.controller;

import com.mahabaleshwermart.common.dto.ApiResponse;
import com.mahabaleshwermart.userservice.dto.UpdateProfileRequest;
import com.mahabaleshwermart.userservice.dto.UserDto;
import com.mahabaleshwermart.userservice.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
@RestController
@RequestMapping({"/api/users", "/api/user-management"})
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile management endpoints")
public class UserController {

    private final UserProfileService userProfileService;

    @RequestMapping(value = "/update-current-user", method = RequestMethod.PUT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update current user profile", description = "Update name, phone and addresses for logged-in user")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(Authentication authentication,
                                                              @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Update profile request by {}", authentication.getName());
        UserDto updated = userProfileService.updateCurrentUser(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    // Aliases to support /profile and /profile/{id} patterns
    @PutMapping("/profile")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update current user profile (alias)", description = "Alias for update-current-user")
    public ResponseEntity<ApiResponse<UserDto>> updateProfileAlias(Authentication authentication,
                                                                   @Valid @RequestBody UpdateProfileRequest request) {
        log.info("[Alias] Update profile request by {}", authentication.getName());
        UserDto updated = userProfileService.updateCurrentUser(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @RequestMapping(value = "/update-user-by-id", method = RequestMethod.PUT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update user profile by ID", description = "Updates the profile of the given user ID if it belongs to the current user")
    public ResponseEntity<ApiResponse<UserDto>> updateProfileById(Authentication authentication,
                                                                  @RequestParam String userId,
                                                                  @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Update profile by id {} requested by {}", userId, authentication.getName());
        UserDto updated = userProfileService.updateUserById(authentication.getName(), userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @PutMapping("/profile/{userId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update user profile by ID (alias)", description = "Alias for update-user-by-id with path variable")
    public ResponseEntity<ApiResponse<UserDto>> updateProfileByIdAlias(Authentication authentication,
                                                                       @PathVariable String userId,
                                                                       @Valid @RequestBody UpdateProfileRequest request) {
        log.info("[Alias] Update profile by id {} requested by {}", userId, authentication.getName());
        UserDto updated = userProfileService.updateUserById(authentication.getName(), userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }
}


