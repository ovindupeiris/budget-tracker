package com.budgettracker.repository;

import com.budgettracker.entity.Subscription;
import com.budgettracker.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Subscription entity
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    /**
     * Find subscriptions by user ID
     */
    List<Subscription> findByUserIdAndDeletedFalse(UUID userId);

    /**
     * Find active subscriptions
     */
    List<Subscription> findByUserIdAndStatusAndDeletedFalse(UUID userId, SubscriptionStatus status);

    /**
     * Find subscriptions by category
     */
    List<Subscription> findByUserIdAndCategoryIdAndDeletedFalse(UUID userId, UUID categoryId);

    /**
     * Find subscriptions due for billing
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId " +
           "AND s.status = 'ACTIVE' " +
           "AND s.nextBillingDate <= :date " +
           "AND s.deleted = false")
    List<Subscription> findSubscriptionsDueForBilling(
        @Param("userId") UUID userId,
        @Param("date") LocalDate date
    );

    /**
     * Find subscriptions needing reminders
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId " +
           "AND s.status = 'ACTIVE' " +
           "AND s.reminderEnabled = true " +
           "AND s.nextBillingDate BETWEEN :startDate AND :endDate " +
           "AND s.deleted = false")
    List<Subscription> findSubscriptionsNeedingReminders(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find subscriptions in free trial
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId " +
           "AND s.freeTrial = true " +
           "AND s.freeTrialEndDate IS NOT NULL " +
           "AND s.deleted = false")
    List<Subscription> findSubscriptionsInTrial(@Param("userId") UUID userId);

    /**
     * Find trials ending soon
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId " +
           "AND s.freeTrial = true " +
           "AND s.freeTrialEndDate BETWEEN :startDate AND :endDate " +
           "AND s.deleted = false")
    List<Subscription> findTrialsEndingSoon(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Count active subscriptions
     */
    long countByUserIdAndStatusAndDeletedFalse(UUID userId, SubscriptionStatus status);
}
