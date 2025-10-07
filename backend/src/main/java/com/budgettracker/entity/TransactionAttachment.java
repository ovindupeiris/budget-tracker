package com.budgettracker.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Transaction Attachment entity
 */
@Entity
@Table(name = "transaction_attachments", indexes = {
    @Index(name = "idx_attachment_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_attachment_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "storage_key", length = 500)
    private String storageKey;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "is_receipt", nullable = false)
    @Builder.Default
    private Boolean isReceipt = false;

    @Column(name = "ocr_extracted", nullable = false)
    @Builder.Default
    private Boolean ocrExtracted = false;

    @Column(name = "ocr_text", columnDefinition = "TEXT")
    private String ocrText;

    @Column(name = "ocr_data", columnDefinition = "JSONB")
    private String ocrData;
}
