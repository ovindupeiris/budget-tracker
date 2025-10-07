package com.budgettracker.entity;

import com.budgettracker.entity.enums.UserRole;
import com.budgettracker.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing application users
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_username", columnList = "username", unique = true),
    @Index(name = "idx_user_status", columnList = "status"),
    @Index(name = "idx_user_created_at", columnList = "created_at")
})
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "username", unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expires_at")
    private LocalDateTime emailVerificationTokenExpiresAt;

    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;

    @Column(name = "password_reset_token_expires_at")
    private LocalDateTime passwordResetTokenExpiresAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "mfa_enabled", nullable = false)
    @Builder.Default
    private Boolean mfaEnabled = false;

    @Column(name = "mfa_secret", length = 255)
    private String mfaSecret;

    @Column(name = "oauth_provider", length = 50)
    private String oauthProvider;

    @Column(name = "oauth_provider_id", length = 255)
    private String oauthProviderId;

    @Embedded
    private UserPreferences preferences;

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(name = "locale", length = 10)
    @Builder.Default
    private String locale = "en";

    @Column(name = "currency_code", length = 3)
    @Builder.Default
    private String currencyCode = "USD";

    @Column(name = "stripe_customer_id", length = 255)
    private String stripeCustomerId;

    @Column(name = "subscription_tier", length = 20)
    @Builder.Default
    private String subscriptionTier = "FREE";

    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    @Column(name = "privacy_accepted_at")
    private LocalDateTime privacyAcceptedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Wallet> wallets = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Budget> budgets = new HashSet<>();

    /**
     * Check if user account is locked
     */
    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Check if user is active and can login
     */
    public boolean canLogin() {
        return status == UserStatus.ACTIVE &&
               emailVerified &&
               !isAccountLocked() &&
               !deleted;
    }

    /**
     * Increment failed login attempts
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    /**
     * Reset failed login attempts
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * Update last login info
     */
    public void updateLastLogin(String ipAddress) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        resetFailedLoginAttempts();
    }
}
