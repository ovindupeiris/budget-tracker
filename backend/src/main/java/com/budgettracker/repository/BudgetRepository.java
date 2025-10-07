package com.budgettracker.repository;

import com.budgettracker.entity.Budget;
import com.budgettracker.entity.enums.BudgetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Budget entity
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    /**
     * Find budgets by user ID
     */
    List<Budget> findByUserIdAndDeletedFalse(UUID userId);

    /**
     * Find active budgets
     */
    List<Budget> findByUserIdAndStatusAndDeletedFalse(UUID userId, BudgetStatus status);

    /**
     * Find budgets by category
     */
    List<Budget> findByUserIdAndCategoryIdAndDeletedFalse(UUID userId, UUID categoryId);

    /**
     * Find budgets by wallet
     */
    List<Budget> findByUserIdAndWalletIdAndDeletedFalse(UUID userId, UUID walletId);

    /**
     * Find active budgets for date
     */
    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
           "AND b.status = 'ACTIVE' " +
           "AND :date BETWEEN b.startDate AND b.endDate " +
           "AND b.deleted = false")
    List<Budget> findActiveBudgetsForDate(
        @Param("userId") UUID userId,
        @Param("date") LocalDate date
    );

    /**
     * Find budgets exceeding threshold
     */
    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
           "AND b.status = 'ACTIVE' " +
           "AND (b.spent / b.amount * 100) >= b.alertThreshold " +
           "AND b.alertEnabled = true " +
           "AND b.alertSent = false " +
           "AND b.deleted = false")
    List<Budget> findBudgetsExceedingThreshold(@Param("userId") UUID userId);

    /**
     * Find exceeded budgets
     */
    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
           "AND b.status = 'ACTIVE' " +
           "AND b.spent > b.amount " +
           "AND b.deleted = false")
    List<Budget> findExceededBudgets(@Param("userId") UUID userId);

    /**
     * Find budgets expiring soon
     */
    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
           "AND b.status = 'ACTIVE' " +
           "AND b.endDate BETWEEN :now AND :threshold " +
           "AND b.deleted = false")
    List<Budget> findBudgetsExpiringSoon(
        @Param("userId") UUID userId,
        @Param("now") LocalDate now,
        @Param("threshold") LocalDate threshold
    );
}
