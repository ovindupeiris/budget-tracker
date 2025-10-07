package com.budgettracker.entity.enums;

/**
 * Transaction types
 */
public enum TransactionType {
    INCOME,         // Income transaction
    EXPENSE,        // Expense transaction
    TRANSFER,       // Transfer between wallets
    INVESTMENT,     // Investment transaction
    DIVIDEND,       // Investment dividend
    INTEREST,       // Interest income
    FEE,            // Fee/charge
    REFUND,         // Refund
    ADJUSTMENT,     // Balance adjustment
    LOAN_PAYMENT,   // Loan payment
    LOAN_DISBURSEMENT  // Loan disbursement
}
