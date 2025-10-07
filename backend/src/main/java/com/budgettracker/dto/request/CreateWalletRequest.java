package com.budgettracker.dto.request;

import com.budgettracker.entity.enums.WalletType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateWalletRequest {
    @NotBlank(message = "Name is requNotificationServiceNotificationServiceNotificationServiceired")
    private String name;
    
    private String description;
    
    @NotNull(message = "Type is required")
    private WalletType type;
    
    @NotBlank(message = "Currency code is required")
    private String currencyCode;
    
    private BigDecimal initialBalance;
    private String icon;
    private String color;
    private Integer displayOrder;
    private Boolean isDefault;
    private Boolean excludeFromTotals;
    
    // Credit card fields
    private BigDecimal creditLimit;
    private Integer billingCycleDay;
    private Integer paymentDueDay;
}
