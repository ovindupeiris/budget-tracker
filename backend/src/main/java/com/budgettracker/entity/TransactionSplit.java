package com.budgettracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Transaction Split entity for multi-category splits
 */
@Entity
@Table(name = "transaction_splits", indexes = {
    @Index(name = "idx_split_transaction_id", columnList = "parent_transaction_id"),
    @Index(name = "idx_split_category_id", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSplit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_transaction_id", nullable = false)
    private Transaction parentTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "split_order")
    private Integer splitOrder;
}
