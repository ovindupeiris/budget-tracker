package com.budgettracker.dto.response;

import com.budgettracker.entity.enums.BudgetPeriod;
import com.budgettracker.entity.enums.BudgetStatus;
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
public class BudgetResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID categoryId;
    private String categoryName;
    private UUID walletId;
    private String walletName;
    private BigDecimal amount;
    private BigDecimal spent;
    private BigDecimal remaining;
    private BigDecimal percentageUsed;
    private String currencyCode;
    private BudgetPeriod period;
    private LocalDate startDate;
    private LocalDate endDate;
    private BudgetStatus status;
    private BigDecimal alertThreshold;
    private Boolean alertEnabled;
    private Boolean alertSent;
    private Boolean rolloverEnabled;
    private LocalDateTime createdAt;
}
