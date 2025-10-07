package com.budgettracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profilePictureUrl;
    private String status;
    private Set<String> roles;
    private Boolean emailVerified;
    private String timezone;
    private String locale;
    private String currencyCode;
    private String subscriptionTier;
    private LocalDateTime subscriptionExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
