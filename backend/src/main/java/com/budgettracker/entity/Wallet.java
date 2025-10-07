package com.budgettracker.entity;

import com.budgettracker.entity.enums.WalletType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Wallet entity representing user accounts/wallets
 */
@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_wallet_user_id", columnList = "user_id"),
    @Index(name = "idx_wallet_type", columnList = "type"),
    @Index(name = "idx_wallet_currency", columnList = "currency_code"),
    @Index(name = "idx_wallet_created_at", columnList = "created_at")
})
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private WalletType type;

    @Column(name = "currency_code", nullable = false, length = 3)
    @Builder.Default
    private String currencyCode = "USD";

    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "initial_balance", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private Boolean isArchived = false;

    @Column(name = "is_shared", nullable = false)
    @Builder.Default
    private Boolean isShared = false;

    @Column(name = "exclude_from_totals", nullable = false)
    @Builder.Default
    private Boolean excludeFromTotals = false;

    // Bank account integration fields
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_number_last_four", length = 4)
    private String accountNumberLastFour;

    @Column(name = "bank_connection_id", length = 255)
    private String bankConnectionId;

    @Column(name = "bank_account_id", length = 255)
    private String bankAccountId;

    @Column(name = "sync_enabled", nullable = false)
    @Builder.Default
    private Boolean syncEnabled = false;

    @Column(name = "last_synced_at")
    private java.time.LocalDateTime lastSyncedAt;

    // Credit card specific fields
    @Column(name = "credit_limit", precision = 19, scale = 4)
    private BigDecimal creditLimit;

    @Column(name = "available_credit", precision = 19, scale = 4)
    private BigDecimal availableCredit;

    @Column(name = "billing_cycle_day")
    private Integer billingCycleDay;

    @Column(name = "payment_due_day")
    private Integer paymentDueDay;

    // Investment account fields
    @Column(name = "account_holder_name", length = 200)
    private String accountHolderName;

    @Column(name = "institution_name", length = 200)
    private String institutionName;

    @Column(name = "account_type", length = 50)
    private String accountType;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Transaction> transactions = new HashSet<>();

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<WalletShare> walletShares = new HashSet<>();

    /**
     * Update wallet balance
     */
    public void updateBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        if (this.creditLimit != null) {
            this.availableCredit = this.creditLimit.subtract(this.balance.abs());
        }
    }

    /**
     * Check if wallet has sufficient balance
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        if (this.type == WalletType.CREDIT_CARD && this.creditLimit != null) {
            return this.availableCredit != null &&
                   this.availableCredit.compareTo(amount) >= 0;
        }
        return this.balance.compareTo(amount) >= 0;
    }

    /**
     * Archive wallet
     */
    public void archive() {
        this.isArchived = true;
    }

    /**
     * Unarchive wallet
     */
    public void unarchive() {
        this.isArchived = false;
    }

    /**
     * Set as default wallet
     */
    public void setAsDefault() {
        this.isDefault = true;
    }
}
