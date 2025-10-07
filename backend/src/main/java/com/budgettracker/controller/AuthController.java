package com.budgettracker.controller;

import com.budgettracker.dto.ApiResponse;
import com.budgettracker.dto.request.LoginRequest;
import com.budgettracker.dto.request.RegisterRequest;
import com.budgettracker.dto.response.AuthResponse;
import com.budgettracker.dto.response.UserResponse;
import com.budgettracker.entity.User;
import com.budgettracker.security.JwtTokenProvider;
import com.budgettracker.security.UserPrincipal;
import com.budgettracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller
 */
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Operation(summary = "Register new user")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(request);

        UserResponse userResponse = mapToUserResponse(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userResponse));
    }

    @Operation(summary = "Login user")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmailOrUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userService.getUserById(userPrincipal.getId());

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs)
                .user(mapToUserResponse(user))
                .build();

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @Operation(summary = "Verify email")
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @Operation(summary = "Initiate password reset")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) {
        userService.initiatePasswordReset(email);
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent", null));
    }

    @Operation(summary = "Reset password")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    @Operation(summary = "Check email availability")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean available = userService.isEmailAvailable(email);
        return ResponseEntity.ok(ApiResponse.success(available));
    }

    @Operation(summary = "Check username availability")
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        boolean available = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(ApiResponse.success(available));
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
