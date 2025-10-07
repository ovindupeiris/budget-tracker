package com.budgettracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private UUID walletId;
    private String walletName;
    private UUID categoryId;
    private String categoryName;
    private String type;
    private BigDecimal amount;
    private String currencyCode;
    private BigDecimal exchangeRate;
    private BigDecimal amountInWalletCurrency;
    private LocalDate transactionDate;
    private String description;
    private String notes;
    private String merchantName;
    private String location;
    private String status;
    private Boolean isReconciled;
    private Boolean hasAttachments;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
