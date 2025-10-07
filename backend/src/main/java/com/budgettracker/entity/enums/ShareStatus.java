package com.budgettracker.entity.enums;

/**
 * Share status
 */
public enum ShareStatus {
    PENDING,        // Invitation sent
    ACCEPTED,       // Invitation accepted
    REJECTED,       // Invitation rejected
    REVOKED,        // Access revoked
    EXPIRED         // Invitation expired
}
