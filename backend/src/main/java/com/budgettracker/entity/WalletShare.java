package com.budgettracker.entity;

import com.budgettracker.entity.enums.SharePermission;
import com.budgettracker.entity.enums.ShareStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Wallet Share entity for shared wallet functionality
 */
@Entity
@Table(name = "wallet_shares", indexes = {
    @Index(name = "idx_wallet_share_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_wallet_share_shared_with_user_id", columnList = "shared_with_user_id"),
    @Index(name = "idx_wallet_share_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_wallet_user_share", columnNames = {"wallet_id", "shared_with_user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletShare extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private User sharedWithUser;

    @Column(name = "shared_by_email", length = 255)
    private String sharedByEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 20)
    @Builder.Default
    private SharePermission permission = SharePermission.VIEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ShareStatus status = ShareStatus.PENDING;

    @Column(name = "invitation_token", length = 255)
    private String invitationToken;

    @Column(name = "invitation_expires_at")
    private java.time.LocalDateTime invitationExpiresAt;

    @Column(name = "accepted_at")
    private java.time.LocalDateTime acceptedAt;

    @Column(name = "can_add_transactions", nullable = false)
    @Builder.Default
    private Boolean canAddTransactions = false;

    @Column(name = "can_edit_transactions", nullable = false)
    @Builder.Default
    private Boolean canEditTransactions = false;

    @Column(name = "can_delete_transactions", nullable = false)
    @Builder.Default
    private Boolean canDeleteTransactions = false;

    @Column(name = "can_manage_budget", nullable = false)
    @Builder.Default
    private Boolean canManageBudget = false;

    /**
     * Accept invitation
     */
    public void accept() {
        this.status = ShareStatus.ACCEPTED;
        this.acceptedAt = java.time.LocalDateTime.now();
    }

    /**
     * Reject invitation
     */
    public void reject() {
        this.status = ShareStatus.REJECTED;
    }

    /**
     * Revoke access
     */
    public void revoke() {
        this.status = ShareStatus.REVOKED;
    }

    /**
     * Check if invitation is valid
     */
    public boolean isInvitationValid() {
        return this.status == ShareStatus.PENDING &&
               this.invitationExpiresAt != null &&
               this.invitationExpiresAt.isAfter(java.time.LocalDateTime.now());
    }
}
