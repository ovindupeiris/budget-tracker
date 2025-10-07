package com.budgettracker.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
    private UUID transactionId;
    private UUID userId;
    private UUID walletId;
    private UUID categoryId;
    private String type;
    private BigDecimal amount;
    private String currencyCode;
    private LocalDate transactionDate;
    private String description;
    private LocalDateTime createdAt;
}
