package com.budgettracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private UUID id;
    private String name;
    private String description;
    private String type;
    private String currencyCode;
    private BigDecimal balance;
    private BigDecimal initialBalance;
    private String icon;
    private String color;
    private Integer displayOrder;
    private Boolean isDefault;
    private Boolean isArchived;
    private Boolean isShared;
    private Boolean excludeFromTotals;

    // Credit card
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;

    // Bank integration
    private Boolean syncEnabled;
    private LocalDateTime lastSyncedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
