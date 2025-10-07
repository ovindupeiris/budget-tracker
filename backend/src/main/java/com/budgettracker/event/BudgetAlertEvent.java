package com.budgettracker.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetAlertEvent {
    private UUID budgetId;
    private UUID userId;
    private String budgetName;
    private BigDecimal amount;
    private BigDecimal spent;
    private BigDecimal threshold;
    private String alertType; // THRESHOLD_REACHED, EXCEEDED
}
