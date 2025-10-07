package com.budgettracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

/**
 * Tag entity for transaction tagging
 */
@Entity
@Table(name = "tags", indexes = {
    @Index(name = "idx_tag_user_id", columnList = "user_id"),
    @Index(name = "idx_tag_name", columnList = "name")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_tag_user_name", columnNames = {"user_id", "name"})
})
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private Set<Transaction> transactions = new HashSet<>();

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    /**
     * Increment usage count
     */
    public void incrementUsageCount() {
        this.usageCount++;
    }

    /**
     * Decrement usage count
     */
    public void decrementUsageCount() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }
}
