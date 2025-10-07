package com.budgettracker.entity;

import com.budgettracker.entity.enums.RecurrenceFrequency;
import com.budgettracker.entity.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Subscription entity for tracking recurring subscriptions and bills
 */
@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_subscription_user_id", columnList = "user_id"),
    @Index(name = "idx_subscription_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_subscription_category_id", columnList = "category_id"),
    @Index(name = "idx_subscription_status", columnList = "status"),
    @Index(name = "idx_subscription_next_billing", columnList = "next_billing_date")
})
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "provider_name", length = 100)
    private String providerName;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_frequency", nullable = false, length = 20)
    private RecurrenceFrequency billingFrequency;

    @Column(name = "billing_day")
    private Integer billingDay;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate;

    @Column(name = "last_billing_date")
    private LocalDate lastBillingDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "reminder_days_before")
    @Builder.Default
    private Integer reminderDaysBefore = 3;

    @Column(name = "reminder_enabled", nullable = false)
    @Builder.Default
    private Boolean reminderEnabled = true;

    @Column(name = "auto_create_transaction", nullable = false)
    @Builder.Default
    private Boolean autoCreateTransaction = true;

    @Column(name = "free_trial", nullable = false)
    @Builder.Default
    private Boolean freeTrial = false;

    @Column(name = "free_trial_end_date")
    private LocalDate freeTrialEndDate;

    @Column(name = "cancellation_notice_period_days")
    private Integer cancellationNoticePeriodDays;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "subscription")
    @Builder.Default
    private Set<Transaction> transactions = new HashSet<>();

    /**
     * Calculate next billing date
     */
    public LocalDate calculateNextBillingDate() {
        LocalDate baseDate = this.nextBillingDate != null ?
                             this.nextBillingDate : this.startDate;

        return switch (this.billingFrequency) {
            case DAILY -> baseDate.plusDays(1);
            case WEEKLY -> baseDate.plusWeeks(1);
            case BIWEEKLY -> baseDate.plusWeeks(2);
            case MONTHLY -> baseDate.plusMonths(1);
            case QUARTERLY -> baseDate.plusMonths(3);
            case YEARLY -> baseDate.plusYears(1);
            default -> baseDate;
        };
    }

    /**
     * Check if reminder should be sent
     */
    public boolean shouldSendReminder() {
        if (!this.reminderEnabled || this.nextBillingDate == null) {
            return false;
        }
        LocalDate reminderDate = this.nextBillingDate.minusDays(this.reminderDaysBefore);
        return LocalDate.now().isEqual(reminderDate) || LocalDate.now().isAfter(reminderDate);
    }

    /**
     * Calculate total spent on subscription
     */
    public BigDecimal calculateTotalSpent() {
        return this.transactions.stream()
                   .map(Transaction::getAmount)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Cancel subscription
     */
    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        this.endDate = LocalDate.now();
    }

    /**
     * Pause subscription
     */
    public void pause() {
        this.status = SubscriptionStatus.PAUSED;
    }

    /**
     * Resume subscription
     */
    public void resume() {
        this.status = SubscriptionStatus.ACTIVE;
    }
}
