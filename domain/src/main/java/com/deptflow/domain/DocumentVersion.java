package com.deptflow.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * An immutable file fact of a {@link Document}. Versions are owned by the
 * document aggregate and carry version-level approval status plus audit fields.
 */
public class DocumentVersion {

    private UUID id;
    private int versionNumber;
    private String storageKey;
    private String storageProvider;
    private String fileName;
    private String contentType;
    private long sizeBytes;
    private String changeNote;
    private UUID createdByPersonId;
    private Instant createdAt;
    private VersionStatus status;
    private Instant submittedAt;
    private UUID submittedByPersonId;
    private Instant reviewedAt;
    private UUID reviewedByPersonId;
    private String reviewComment;
    private Document document;

    /** For persistence tooling. */
    protected DocumentVersion() {
    }

    DocumentVersion(int versionNumber, String storageKey, String storageProvider, String fileName,
                    String contentType, long sizeBytes, String changeNote, UUID createdByPersonId) {
        if (versionNumber < 1) {
            throw new DomainException("versionNumber must be positive");
        }
        if (sizeBytes < 0) {
            throw new DomainException("sizeBytes cannot be negative");
        }
        this.id = UUID.randomUUID();
        this.versionNumber = versionNumber;
        this.storageKey = DomainAssertions.requireText(storageKey, "storageKey");
        this.storageProvider = storageProvider;
        this.fileName = DomainAssertions.requireText(fileName, "fileName");
        this.contentType = DomainAssertions.requireText(contentType, "contentType");
        this.sizeBytes = sizeBytes;
        this.changeNote = changeNote;
        this.createdByPersonId = createdByPersonId;
        this.createdAt = Instant.now();
        this.status = VersionStatus.DRAFT;
    }

    public UUID getId() {
        return id;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getStorageProvider() {
        return storageProvider;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getChangeNote() {
        return changeNote;
    }

    public UUID getCreatedByPersonId() {
        return createdByPersonId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public VersionStatus getStatus() {
        return status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public UUID getSubmittedByPersonId() {
        return submittedByPersonId;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public UUID getReviewedByPersonId() {
        return reviewedByPersonId;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    void submit(UUID byPersonId) {
        if (status != VersionStatus.DRAFT && status != VersionStatus.REJECTED) {
            throw new DomainException("Only Draft or Rejected versions can be submitted for approval");
        }
        this.status = VersionStatus.PENDING_APPROVAL;
        this.submittedAt = Instant.now();
        this.submittedByPersonId = byPersonId;
    }

    void approve(UUID byPersonId) {
        if (status != VersionStatus.PENDING_APPROVAL) {
            throw new DomainException("Only PendingApproval versions can be approved");
        }
        this.status = VersionStatus.APPROVED;
        this.reviewedAt = Instant.now();
        this.reviewedByPersonId = byPersonId;
    }

    void reject(UUID byPersonId, String comment) {
        if (status != VersionStatus.PENDING_APPROVAL) {
            throw new DomainException("Only PendingApproval versions can be rejected");
        }
        if (comment == null || comment.isBlank()) {
            throw new DomainException("A rejection requires a comment");
        }
        this.status = VersionStatus.REJECTED;
        this.reviewedAt = Instant.now();
        this.reviewedByPersonId = byPersonId;
        this.reviewComment = comment.trim();
    }

    Document getDocument() {
        return document;
    }

    void setDocument(Document document) {
        this.document = document;
    }
}
