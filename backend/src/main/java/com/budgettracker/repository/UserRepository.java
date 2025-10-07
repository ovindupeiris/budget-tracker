package com.budgettracker.repository;

import com.budgettracker.entity.User;
import com.budgettracker.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email or username
     */
    Optional<User> findByEmailOrUsername(String email, String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Find by email verification token
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Find by password reset token
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Find by OAuth provider and ID
     */
    Optional<User> findByOauthProviderAndOauthProviderId(String provider, String providerId);

    /**
     * Find users by status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Find users by subscription tier
     */
    List<User> findBySubscriptionTier(String subscriptionTier);

    /**
     * Find users with expired subscriptions
     */
    @Query("SELECT u FROM User u WHERE u.subscriptionExpiresAt < :now AND u.deleted = false")
    List<User> findUsersWithExpiredSubscriptions(@Param("now") LocalDateTime now);

    /**
     * Find users by Stripe customer ID
     */
    Optional<User> findByStripeCustomerId(String stripeCustomerId);

    /**
     * Find active users who logged in recently
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.lastLoginAt >= :since AND u.deleted = false")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);

    /**
     * Count active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE' AND u.deleted = false")
    long countActiveUsers();

    /**
     * Find users with MFA enabled
     */
    @Query("SELECT u FROM User u WHERE u.mfaEnabled = true AND u.deleted = false")
    List<User> findUsersWithMfaEnabled();
}
