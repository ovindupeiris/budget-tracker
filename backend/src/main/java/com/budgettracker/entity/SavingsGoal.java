package com.budgettracker.entity;

import com.budgettracker.entity.enums.GoalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Savings Goal entity
 */
@Entity
@Table(name = "savings_goals", indexes = {
    @Index(name = "idx_goal_user_id", columnList = "user_id"),
    @Index(name = "idx_goal_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_goal_status", columnList = "status"),
    @Index(name = "idx_goal_target_date", columnList = "target_date")
})
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "target_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "auto_save_enabled", nullable = false)
    @Builder.Default
    private Boolean autoSaveEnabled = false;

    @Column(name = "auto_save_amount", precision = 19, scale = 4)
    private BigDecimal autoSaveAmount;

    @Column(name = "auto_save_frequency", length = 20)
    private String autoSaveFrequency;

    @Column(name = "completed_at")
    private java.time.LocalDateTime completedAt;

    /**
     * Get remaining amount to reach goal
     */
    public BigDecimal getRemainingAmount() {
        return this.targetAmount.subtract(this.currentAmount);
    }

    /**
     * Get percentage complete
     */
    public BigDecimal getPercentageComplete() {
        if (this.targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return this.currentAmount.divide(this.targetAmount, 4, java.math.RoundingMode.HALF_UP)
                   .multiply(new BigDecimal("100"));
    }

    /**
     * Check if goal is completed
     */
    public boolean isCompleted() {
        return this.currentAmount.compareTo(this.targetAmount) >= 0;
    }

    /**
     * Add amount to goal
     */
    public void addAmount(BigDecimal amount) {
        this.currentAmount = this.currentAmount.add(amount);
        if (isCompleted() && this.status == GoalStatus.ACTIVE) {
            this.status = GoalStatus.COMPLETED;
            this.completedAt = java.time.LocalDateTime.now();
        }
    }

    /**
     * Calculate days until target date
     */
    public Long getDaysUntilTarget() {
        if (this.targetDate == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), this.targetDate);
    }
}
