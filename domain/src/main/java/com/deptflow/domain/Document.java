package com.deptflow.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * A department-owned document. {@code Document} is the aggregate root that owns
 * its {@link DocumentVersion}s and enforces the approval state machine; the
 * summary {@link #getStatus()} is derived from the latest version and mutated
 * only here.
 */
public class Document {

    private UUID id;
    private UUID institutionId;
    private UUID departmentId;
    private UUID documentTypeId;
    private String title;
    private String description;
    private DocumentStatus status;
    private UUID createdByPersonId;
    private Instant createdAt;
    private Instant updatedAt;
    private List<DocumentVersion> versions = new ArrayList<>();

    /** For persistence tooling. */
    protected Document() {
    }

    private Document(UUID id, UUID institutionId, UUID departmentId, UUID documentTypeId,
                     String title, String description, UUID createdByPersonId) {
        this.id = id;
        this.institutionId = DomainAssertions.requireId(institutionId, "institutionId");
        this.departmentId = DomainAssertions.requireId(departmentId, "departmentId");
        this.documentTypeId = documentTypeId;
        this.title = DomainAssertions.requireText(title, "title");
        this.description = description;
        this.createdByPersonId = createdByPersonId;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.status = DocumentStatus.DRAFT;
    }

    public static Document create(UUID institutionId, UUID departmentId, UUID documentTypeId,
                                  String title, String description, UUID createdByPersonId) {
        return new Document(UUID.randomUUID(), institutionId, departmentId, documentTypeId,
                title, description, createdByPersonId);
    }

    public UUID getId() {
        return id;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public UUID getDocumentTypeId() {
        return documentTypeId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public UUID getCreatedByPersonId() {
        return createdByPersonId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<DocumentVersion> getVersions() {
        return List.copyOf(versions);
    }

    public DocumentVersion getLatestVersion() {
        return versions.stream()
                .max(Comparator.comparingInt(DocumentVersion::getVersionNumber))
                .orElse(null);
    }

    // ---- lifecycle behavior ----

    public DocumentVersion addVersion(String storageKey, String storageProvider, String fileName,
                                      String contentType, long sizeBytes, String changeNote, UUID createdByPersonId) {
        ensureNotArchived();
        int next = versions.stream()
                .mapToInt(DocumentVersion::getVersionNumber)
                .max()
                .orElse(0) + 1;
        DocumentVersion version = new DocumentVersion(next, storageKey, storageProvider, fileName,
                contentType, sizeBytes, changeNote, createdByPersonId);
        versions.add(version);
        refreshSummaryStatus();
        touch();
        return version;
    }

    public void submit(UUID byPersonId) {
        requireLatest().submit(byPersonId);
        refreshSummaryStatus();
        touch();
    }

    public void approve(UUID byPersonId) {
        requireLatest().approve(byPersonId);
        refreshSummaryStatus();
        touch();
    }

    public void reject(UUID byPersonId, String comment) {
        requireLatest().reject(byPersonId, comment);
        refreshSummaryStatus();
        touch();
    }

    public void archive(UUID byPersonId) {
        boolean hasApproved = versions.stream().anyMatch(v -> v.getStatus() == VersionStatus.APPROVED);
        if (!hasApproved) {
            throw new DomainException("A document can only be archived once it has an Approved version");
        }
        this.status = DocumentStatus.ARCHIVED;
        touch();
    }

    public void updateMetadata(String title, String description, UUID documentTypeId) {
        ensureNotArchived();
        this.title = DomainAssertions.requireText(title, "title");
        this.description = description;
        this.documentTypeId = documentTypeId;
        touch();
    }

    private void refreshSummaryStatus() {
        if (status == DocumentStatus.ARCHIVED) {
            return;
        }
        DocumentVersion latest = getLatestVersion();
        if (latest == null) {
            this.status = DocumentStatus.DRAFT;
            return;
        }
        this.status = switch (latest.getStatus()) {
            case DRAFT, REJECTED -> DocumentStatus.DRAFT;
            case PENDING_APPROVAL -> DocumentStatus.PENDING_APPROVAL;
            case APPROVED -> DocumentStatus.PUBLISHED;
        };
    }

    private DocumentVersion requireLatest() {
        DocumentVersion latest = getLatestVersion();
        if (latest == null) {
            throw new DomainException("Document has no versions");
        }
        return latest;
    }

    private void ensureNotArchived() {
        if (status == DocumentStatus.ARCHIVED) {
            throw new DomainException("An archived document is immutable");
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
