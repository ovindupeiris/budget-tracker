package com.budgettracker.dto.request;

import com.budgettracker.entity.enums.BudgetPeriod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateBudgetRequest {
    @NotBlank(message = "Budget name is required")
    private String name;

    private String description;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Currency code is required")
    private String currencyCode;

    @NotNull(message = "Period is required")
    private BudgetPeriod period;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private UUID categoryId;
    private UUID walletId;
    private BigDecimal alertThreshold = new BigDecimal("80.00");
    private Boolean alertEnabled = true;
    private Boolean rolloverEnabled = false;
}
