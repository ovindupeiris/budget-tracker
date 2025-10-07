package com.budgettracker.entity;

import com.budgettracker.entity.enums.RuleCondition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

/**
 * Category Rule entity for auto-categorization
 */
@Entity
@Table(name = "category_rules", indexes = {
    @Index(name = "idx_rule_user_id", columnList = "user_id"),
    @Index(name = "idx_rule_category_id", columnList = "category_id"),
    @Index(name = "idx_rule_priority", columnList = "priority"),
    @Index(name = "idx_rule_active", columnList = "is_active")
})
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false, length = 30)
    private RuleCondition condition;

    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName;

    @Column(name = "field_value", nullable = false, length = 500)
    private String fieldValue;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "apply_count")
    @Builder.Default
    private Integer applyCount = 0;

    /**
     * Check if rule matches transaction
     */
    public boolean matches(Transaction transaction) {
        if (!this.isActive) {
            return false;
        }

        String valueToCheck = switch (this.fieldName.toLowerCase()) {
            case "description" -> transaction.getDescription();
            case "merchant" -> transaction.getMerchantName();
            case "amount" -> transaction.getAmount().toString();
            case "location" -> transaction.getLocation();
            default -> null;
        };

        if (valueToCheck == null) {
            return false;
        }

        return switch (this.condition) {
            case CONTAINS -> valueToCheck.toLowerCase().contains(this.fieldValue.toLowerCase());
            case EQUALS -> valueToCheck.equalsIgnoreCase(this.fieldValue);
            case STARTS_WITH -> valueToCheck.toLowerCase().startsWith(this.fieldValue.toLowerCase());
            case ENDS_WITH -> valueToCheck.toLowerCase().endsWith(this.fieldValue.toLowerCase());
            case REGEX -> valueToCheck.matches(this.fieldValue);
            default -> false;
        };
    }

    /**
     * Increment apply count
     */
    public void incrementApplyCount() {
        this.applyCount++;
    }
}
