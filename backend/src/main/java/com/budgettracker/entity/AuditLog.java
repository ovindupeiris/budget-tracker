package com.budgettracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit Log entity for tracking system events
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_entity_type", columnList = "entity_type"),
    @Index(name = "idx_audit_entity_id", columnList = "entity_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_ip_address", columnList = "ip_address")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "old_value", columnDefinition = "JSONB")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "JSONB")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "success", nullable = false)
    @Builder.Default
    private Boolean success = true;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    @Column(name = "request_id", length = 100)
    private String requestId;
}
