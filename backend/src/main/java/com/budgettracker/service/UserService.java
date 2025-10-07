package com.budgettracker.service;

import com.budgettracker.dto.request.RegisterRequest;
import com.budgettracker.dto.request.UpdateUserRequest;
import com.budgettracker.dto.response.UserResponse;
import com.budgettracker.entity.User;
import com.budgettracker.entity.enums.UserRole;
import com.budgettracker.entity.enums.UserStatus;
import com.budgettracker.exception.BusinessException;
import com.budgettracker.exception.ResourceNotFoundException;
import com.budgettracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Service for user management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new user
     */
    @Transactional
    public User registerUser(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use", "EMAIL_EXISTS");
        }

        // Check if username already exists (if provided)
        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already in use", "USERNAME_EXISTS");
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .status(UserStatus.PENDING)
                .emailVerified(false)
                .roles(new HashSet<>(Set.of(UserRole.USER)))
                .currencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode() : "USD")
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .locale(request.getLocale() != null ? request.getLocale() : "en")
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        return user;
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Update user profile
     */
    @Transactional
    public User updateUser(UUID userId, UpdateUserRequest request) {
        User user = getUserById(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getTimezone() != null) {
            user.setTimezone(request.getTimezone());
        }
        if (request.getLocale() != null) {
            user.setLocale(request.getLocale());
        }
        if (request.getCurrencyCode() != null) {
            user.setCurrencyCode(request.getCurrencyCode());
        }

        user = userRepository.save(user);
        log.info("User updated successfully: {}", user.getId());

        return user;
    }

    /**
     * Verify user email
     */
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BusinessException("Invalid verification token", "INVALID_TOKEN"));

        if (user.getEmailVerificationTokenExpiresAt() != null &&
            user.getEmailVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Verification token has expired", "TOKEN_EXPIRED");
        }

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiresAt(null);

        userRepository.save(user);
        log.info("Email verified for user: {}", user.getEmail());
    }

    /**
     * Update user password
     */
    @Transactional
    public void updatePassword(UUID userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect", "INVALID_PASSWORD");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated for user: {}", user.getId());
    }

    /**
     * Initiate password reset
     */
    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(24));

        userRepository.save(user);
        log.info("Password reset initiated for user: {}", email);

        // TODO: Send password reset email
    }

    /**
     * Reset password with token
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new BusinessException("Invalid reset token", "INVALID_TOKEN"));

        if (user.getPasswordResetTokenExpiresAt() != null &&
            user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Reset token has expired", "TOKEN_EXPIRED");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);

        userRepository.save(user);
        log.info("Password reset for user: {}", user.getEmail());
    }

    /**
     * Deactivate user account
     */
    @Transactional
    public void deactivateUser(UUID userId) {
        User user = getUserById(userId);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("User deactivated: {}", userId);
    }

    /**
     * Delete user account (soft delete)
     */
    @Transactional
    public void deleteUser(UUID userId) {
        User user = getUserById(userId);
        user.softDelete();
        userRepository.save(user);
        log.info("User deleted (soft): {}", userId);
    }

    /**
     * Check if user exists
     */
    @Transactional(readOnly = true)
    public boolean userExists(UUID userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Check if email is available
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * Check if username is available
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
}
