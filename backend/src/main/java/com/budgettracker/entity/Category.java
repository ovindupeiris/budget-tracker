package com.budgettracker.entity;

import com.budgettracker.entity.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

/**
 * Category entity for transaction categorization
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_user_id", columnList = "user_id"),
    @Index(name = "idx_category_type", columnList = "type"),
    @Index(name = "idx_category_parent_id", columnList = "parent_category_id"),
    @Index(name = "idx_category_system", columnList = "is_system")
})
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CategoryType type;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Category> subcategories = new HashSet<>();

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private Set<Transaction> transactions = new HashSet<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Budget> budgets = new HashSet<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CategoryRule> categoryRules = new HashSet<>();

    /**
     * Check if this is a parent category
     */
    public boolean isParentCategory() {
        return this.parentCategory == null;
    }

    /**
     * Check if this is a subcategory
     */
    public boolean isSubcategory() {
        return this.parentCategory != null;
    }

    /**
     * Add subcategory
     */
    public void addSubcategory(Category subcategory) {
        this.subcategories.add(subcategory);
        subcategory.setParentCategory(this);
    }

    /**
     * Get full category path (Parent > Child)
     */
    public String getFullPath() {
        if (this.parentCategory != null) {
            return this.parentCategory.getName() + " > " + this.name;
        }
        return this.name;
    }
}
