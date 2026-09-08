package com.deptflow.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * A configurable per-institution organizational role. Distinct from any
 * authentication-level authority; describes what a person does within a
 * department.
 */
public class Role {

    private UUID id;
    private UUID institutionId;
    private String name;
    private String description;
    private boolean active;
    private boolean canApproveDocuments;
    private Instant createdAt;
    private Instant updatedAt;

    /** For persistence tooling. */
    protected Role() {
    }

    private Role(UUID id, UUID institutionId, String name, String description) {
        this.id = id;
        this.institutionId = DomainAssertions.requireId(institutionId, "institutionId");
        this.name = DomainAssertions.requireText(name, "name");
        this.description = description;
        this.active = true;
        this.canApproveDocuments = false;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Role create(UUID institutionId, String name, String description) {
        return new Role(UUID.randomUUID(), institutionId, name, description);
    }

    public UUID getId() {
        return id;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isCanApproveDocuments() {
        return canApproveDocuments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setCanApproveDocuments(boolean canApproveDocuments) {
        this.canApproveDocuments = canApproveDocuments;
        touch();
    }

    public void rename(String name, String description) {
        this.name = DomainAssertions.requireText(name, "name");
        this.description = description;
        touch();
    }

    public void archive() {
        this.active = false;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
