package com.budgettracker.entity;

import com.budgettracker.entity.enums.BudgetPeriod;
import com.budgettracker.entity.enums.BudgetStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Budget entity for budget management
 */
@Entity
@Table(name = "budgets", indexes = {
    @Index(name = "idx_budget_user_id", columnList = "user_id"),
    @Index(name = "idx_budget_category_id", columnList = "category_id"),
    @Index(name = "idx_budget_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_budget_period", columnList = "period"),
    @Index(name = "idx_budget_dates", columnList = "start_date,end_date"),
    @Index(name = "idx_budget_status", columnList = "status")
})
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "spent", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal spent = BigDecimal.ZERO;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false, length = 20)
    private BudgetPeriod period;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BudgetStatus status = BudgetStatus.ACTIVE;

    @Column(name = "alert_threshold", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal alertThreshold = new BigDecimal("80.00");

    @Column(name = "alert_enabled", nullable = false)
    @Builder.Default
    private Boolean alertEnabled = true;

    @Column(name = "alert_sent", nullable = false)
    @Builder.Default
    private Boolean alertSent = false;

    @Column(name = "rollover_enabled", nullable = false)
    @Builder.Default
    private Boolean rolloverEnabled = false;

    @Column(name = "rollover_amount", precision = 19, scale = 4)
    private BigDecimal rolloverAmount;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "color", length = 20)
    private String color;

    /**
     * Get remaining budget amount
     */
    public BigDecimal getRemaining() {
        return this.amount.subtract(this.spent);
    }

    /**
     * Get percentage spent
     */
    public BigDecimal getPercentageSpent() {
        if (this.amount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return this.spent.divide(this.amount, 4, java.math.RoundingMode.HALF_UP)
                   .multiply(new BigDecimal("100"));
    }

    /**
     * Check if budget is exceeded
     */
    public boolean isExceeded() {
        return this.spent.compareTo(this.amount) > 0;
    }

    /**
     * Check if alert threshold is reached
     */
    public boolean isAlertThresholdReached() {
        BigDecimal percentage = getPercentageSpent();
        return percentage.compareTo(this.alertThreshold) >= 0;
    }

    /**
     * Update spent amount
     */
    public void updateSpent(BigDecimal transactionAmount) {
        this.spent = this.spent.add(transactionAmount);
    }

    /**
     * Check if budget is active for given date
     */
    public boolean isActiveForDate(LocalDate date) {
        return this.status == BudgetStatus.ACTIVE &&
               !date.isBefore(this.startDate) &&
               !date.isAfter(this.endDate);
    }

    /**
     * Reset budget for new period
     */
    public void resetForNewPeriod(LocalDate newStartDate, LocalDate newEndDate) {
        if (this.rolloverEnabled && this.spent.compareTo(this.amount) < 0) {
            this.rolloverAmount = this.amount.subtract(this.spent);
        }
        this.spent = BigDecimal.ZERO;
        this.startDate = newStartDate;
        this.endDate = newEndDate;
        this.alertSent = false;
    }
}
