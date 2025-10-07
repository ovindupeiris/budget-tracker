package com.budgettracker.entity.enums;

/**
 * Transaction status
 */
public enum TransactionStatus {
    PENDING,        // Pending transaction
    COMPLETED,      // Completed transaction
    CANCELLED,      // Cancelled transaction
    FAILED,         // Failed transaction
    SCHEDULED,      // Scheduled for future
    PROCESSING      // Being processed
}
