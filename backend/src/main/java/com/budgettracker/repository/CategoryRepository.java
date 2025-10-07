package com.budgettracker.repository;

import com.budgettracker.entity.Category;
import com.budgettracker.entity.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Find categories by user ID
     */
    List<Category> findByUserIdAndDeletedFalse(UUID userId);

    /**
     * Find system categories
     */
    List<Category> findByIsSystemTrueAndDeletedFalse();

    /**
     * Find categories by type
     */
    List<Category> findByUserIdAndTypeAndDeletedFalse(UUID userId, CategoryType type);

    /**
     * Find parent categories (top-level)
     */
    List<Category> findByUserIdAndParentCategoryIdIsNullAndDeletedFalse(UUID userId);

    /**
     * Find subcategories
     */
    List<Category> findByParentCategoryIdAndDeletedFalse(UUID parentCategoryId);

    /**
     * Find category by name
     */
    Optional<Category> findByUserIdAndNameAndDeletedFalse(UUID userId, String name);

    /**
     * Find active categories
     */
    List<Category> findByUserIdAndIsActiveTrueAndDeletedFalse(UUID userId);

    /**
     * Get all categories for user (including system)
     */
    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.isSystem = true) " +
           "AND c.deleted = false ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findAllCategoriesForUser(@Param("userId") UUID userId);

    /**
     * Count user categories
     */
    long countByUserIdAndDeletedFalse(UUID userId);

    /**
     * Check if category name exists
     */
    boolean existsByUserIdAndNameAndDeletedFalse(UUID userId, String name);
}
