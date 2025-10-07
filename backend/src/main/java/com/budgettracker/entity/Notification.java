package com.budgettracker.entity;

import com.budgettracker.entity.enums.NotificationChannel;
import com.budgettracker.entity.enums.NotificationStatus;
import com.budgettracker.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Notification entity
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user_id", columnList = "user_id"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_status", columnList = "status"),
    @Index(name = "idx_notification_created_at", columnList = "created_at"),
    @Index(name = "idx_notification_read", columnList = "is_read")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "related_entity_type", length = 100)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private java.util.UUID relatedEntityId;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * Mark as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Mark as sent
     */
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Mark as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    /**
     * Check if should retry
     */
    public boolean shouldRetry() {
        return this.status == NotificationStatus.FAILED &&
               this.retryCount < this.maxRetries;
    }
}
