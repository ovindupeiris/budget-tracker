package com.budgettracker.entity.enums;

/**
 * User account status
 */
public enum UserStatus {
    ACTIVE,         // Active account
    INACTIVE,       // Inactive account
    SUSPENDED,      // Suspended by admin
    PENDING,        // Pending email verification
    CLOSED          // Account closed by user
}
