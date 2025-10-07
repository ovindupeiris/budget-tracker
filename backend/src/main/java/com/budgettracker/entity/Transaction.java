package com.budgettracker.entity;

import com.budgettracker.entity.enums.TransactionStatus;
import com.budgettracker.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Transaction entity representing financial transactions
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_transaction_category_id", columnList = "category_id"),
    @Index(name = "idx_transaction_type", columnList = "type"),
    @Index(name = "idx_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_created_at", columnList = "created_at"),
    @Index(name = "idx_transaction_user_date", columnList = "user_id,transaction_date")
})
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TransactionType type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "exchange_rate", precision = 19, scale = 8)
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "amount_in_wallet_currency", precision = 19, scale = 4)
    private BigDecimal amountInWalletCurrency;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "merchant_name", length = 200)
    private String merchantName;

    @Column(name = "location", length = 300)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.COMPLETED;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_template_id")
    private RecurringTransaction recurringTemplate;

    @Column(name = "is_reconciled", nullable = false)
    @Builder.Default
    private Boolean isReconciled = false;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    // Transfer-specific fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet_id")
    private Wallet fromWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_id")
    private Wallet toWallet;

    @Column(name = "transfer_fee", precision = 19, scale = 4)
    private BigDecimal transferFee;

    @Column(name = "linked_transaction_id")
    private java.util.UUID linkedTransactionId;

    // Bank sync fields
    @Column(name = "bank_transaction_id", length = 255)
    private String bankTransactionId;

    @Column(name = "bank_imported", nullable = false)
    @Builder.Default
    private Boolean bankImported = false;

    @Column(name = "bank_imported_at")
    private LocalDateTime bankImportedAt;

    @Column(name = "bank_pending", nullable = false)
    @Builder.Default
    private Boolean bankPending = false;

    // Receipt and attachments
    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "has_attachments", nullable = false)
    @Builder.Default
    private Boolean hasAttachments = false;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TransactionAttachment> attachments = new HashSet<>();

    // Tags
    @ManyToMany
    @JoinTable(
        name = "transaction_tags",
        joinColumns = @JoinColumn(name = "transaction_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // Split transactions
    @Column(name = "is_split", nullable = false)
    @Builder.Default
    private Boolean isSplit = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_transaction_id")
    private Transaction parentTransaction;

    @OneToMany(mappedBy = "parentTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TransactionSplit> splits = new HashSet<>();

    // Subscription/Bill tracking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    // Investment tracking
    @Column(name = "investment_quantity", precision = 19, scale = 8)
    private BigDecimal investmentQuantity;

    @Column(name = "investment_price_per_unit", precision = 19, scale = 8)
    private BigDecimal investmentPricePerUnit;

    @Column(name = "investment_symbol", length = 20)
    private String investmentSymbol;

    // Categorization
    @Column(name = "auto_categorized", nullable = false)
    @Builder.Default
    private Boolean autoCategorized = false;

    @Column(name = "ml_confidence_score", precision = 5, scale = 4)
    private BigDecimal mlConfidenceScore;

    // Metadata
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "external_id", length = 255)
    private String externalId;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    // Offline sync
    @Column(name = "sync_status", length = 20)
    @Builder.Default
    private String syncStatus = "SYNCED";

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "conflict_resolved", nullable = false)
    @Builder.Default
    private Boolean conflictResolved = true;

    /**
     * Mark transaction as reconciled
     */
    public void reconcile() {
        this.isReconciled = true;
        this.reconciledAt = LocalDateTime.now();
    }

    /**
     * Add tag to transaction
     */
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    /**
     * Remove tag from transaction
     */
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    /**
     * Add attachment to transaction
     */
    public void addAttachment(TransactionAttachment attachment) {
        this.attachments.add(attachment);
        attachment.setTransaction(this);
        this.hasAttachments = true;
    }

    /**
     * Calculate final amount considering exchange rate
     */
    @PrePersist
    @PreUpdate
    public void calculateAmountInWalletCurrency() {
        if (this.exchangeRate != null && this.amount != null) {
            this.amountInWalletCurrency = this.amount.multiply(this.exchangeRate);
        } else {
            this.amountInWalletCurrency = this.amount;
        }
    }
}
