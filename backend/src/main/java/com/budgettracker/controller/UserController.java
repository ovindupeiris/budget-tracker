package com.budgettracker.controller;

import com.budgettracker.dto.ApiResponse;
import com.budgettracker.dto.request.UpdateUserRequest;
import com.budgettracker.dto.response.UserResponse;
import com.budgettracker.entity.User;
import com.budgettracker.security.UserPrincipal;
import com.budgettracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User", description = "User management endpoints")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.getUserById(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(mapToUserResponse(user)));
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", mapToUserResponse(user)));
    }

    @Operation(summary = "Update password")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<String>> updatePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        userService.updatePassword(userPrincipal.getId(), oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", null));
    }

    @Operation(summary = "Deactivate account")
    @PostMapping("/me/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        userService.deactivateUser(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deactivated successfully", null));
    }

    @Operation(summary = "Delete account")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        userService.deleteUser(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .status(user.getStatus().name())
                .roles(user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()))
                .emailVerified(user.getEmailVerified())
                .timezone(user.getTimezone())
                .locale(user.getLocale())
                .currencyCode(user.getCurrencyCode())
                .subscriptionTier(user.getSubscriptionTier())
                .subscriptionExpiresAt(user.getSubscriptionExpiresAt())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
