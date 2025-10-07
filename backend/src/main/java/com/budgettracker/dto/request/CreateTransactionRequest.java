package com.budgettracker.dto.request;

import com.budgettracker.entity.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
public class CreateTransactionRequest {
    @NotNull(message = "Wallet ID is required")
    private UUID walletId;
    
    private UUID categoryId;
    
    @NotNull(message = "Type is required")
    private TransactionType type;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotNull(message = "Currency code is required")
    private String currencyCode;
    
    private BigDecimal exchangeRate;
    
    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;
    
    private String description;
    private String notes;
    private String merchantName;
    private String location;
    
    // Transfer fields
    private UUID toWalletId;
    private BigDecimal transferFee;
    
    // Tags
    private Set<UUID> tagIds;
    
    // Recurring
    private Boolean isRecurring;
    private UUID recurringTemplateId;
}
