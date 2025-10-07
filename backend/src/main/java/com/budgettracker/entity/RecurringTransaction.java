package com.budgettracker.entity;

import com.budgettracker.entity.enums.RecurrenceFrequency;
import com.budgettracker.entity.enums.RecurringStatus;
import com.budgettracker.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Recurring Transaction template entity
 */
@Entity
@Table(name = "recurring_transactions", indexes = {
    @Index(name = "idx_recurring_user_id", columnList = "user_id"),
    @Index(name = "idx_recurring_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_recurring_status", columnList = "status"),
    @Index(name = "idx_recurring_next_date", columnList = "next_occurrence_date")
})
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringTransaction extends BaseEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TransactionType type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 20)
    private RecurrenceFrequency frequency;

    @Column(name = "interval_count")
    @Builder.Default
    private Integer intervalCount = 1;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "next_occurrence_date")
    private LocalDate nextOccurrenceDate;

    @Column(name = "last_occurrence_date")
    private LocalDate lastOccurrenceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RecurringStatus status = RecurringStatus.ACTIVE;

    @Column(name = "auto_create", nullable = false)
    @Builder.Default
    private Boolean autoCreate = true;

    @Column(name = "notification_days_before")
    @Builder.Default
    private Integer notificationDaysBefore = 1;

    @Column(name = "notification_enabled", nullable = false)
    @Builder.Default
    private Boolean notificationEnabled = true;

    @Column(name = "occurrence_count")
    @Builder.Default
    private Integer occurrenceCount = 0;

    @Column(name = "max_occurrences")
    private Integer maxOccurrences;

    @OneToMany(mappedBy = "recurringTemplate")
    @Builder.Default
    private Set<Transaction> generatedTransactions = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "recurring_transaction_tags",
        joinColumns = @JoinColumn(name = "recurring_transaction_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    /**
     * Check if should create next occurrence
     */
    public boolean shouldCreateNextOccurrence() {
        if (this.status != RecurringStatus.ACTIVE || !this.autoCreate) {
            return false;
        }
        if (this.nextOccurrenceDate == null || this.nextOccurrenceDate.isAfter(LocalDate.now())) {
            return false;
        }
        if (this.maxOccurrences != null && this.occurrenceCount >= this.maxOccurrences) {
            return false;
        }
        if (this.endDate != null && LocalDate.now().isAfter(this.endDate)) {
            return false;
        }
        return true;
    }

    /**
     * Calculate next occurrence date
     */
    public LocalDate calculateNextOccurrenceDate() {
        LocalDate baseDate = this.nextOccurrenceDate != null ?
                             this.nextOccurrenceDate : this.startDate;

        return switch (this.frequency) {
            case DAILY -> baseDate.plusDays(this.intervalCount);
            case WEEKLY -> baseDate.plusWeeks(this.intervalCount);
            case BIWEEKLY -> baseDate.plusWeeks(2L * this.intervalCount);
            case MONTHLY -> baseDate.plusMonths(this.intervalCount);
            case QUARTERLY -> baseDate.plusMonths(3L * this.intervalCount);
            case YEARLY -> baseDate.plusYears(this.intervalCount);
            default -> baseDate;
        };
    }

    /**
     * Mark occurrence as created
     */
    public void markOccurrenceCreated() {
        this.lastOccurrenceDate = this.nextOccurrenceDate;
        this.nextOccurrenceDate = calculateNextOccurrenceDate();
        this.occurrenceCount++;

        if (this.maxOccurrences != null && this.occurrenceCount >= this.maxOccurrences) {
            this.status = RecurringStatus.COMPLETED;
        } else if (this.endDate != null && this.nextOccurrenceDate.isAfter(this.endDate)) {
            this.status = RecurringStatus.COMPLETED;
        }
    }

    /**
     * Pause recurring transaction
     */
    public void pause() {
        this.status = RecurringStatus.PAUSED;
    }

    /**
     * Resume recurring transaction
     */
    public void resume() {
        this.status = RecurringStatus.ACTIVE;
    }
}
