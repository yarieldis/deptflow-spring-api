package com.deptflow.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Top-level organization and multi-tenant boundary. Institutions may form an
 * optional parent/child umbrella hierarchy.
 */
public class Institution {

    private UUID id;
    private UUID parentInstitutionId;
    private String name;
    private String code;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    /** For persistence tooling. */
    protected Institution() {
    }

    private Institution(UUID id, UUID parentInstitutionId, String name, String code) {
        this.id = id;
        this.parentInstitutionId = parentInstitutionId;
        this.name = DomainAssertions.requireText(name, "name");
        this.code = DomainAssertions.normalizeCode(code);
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Institution create(String name, String code) {
        return create(null, name, code);
    }

    public static Institution create(UUID parentInstitutionId, String name, String code) {
        return new Institution(UUID.randomUUID(), parentInstitutionId, name, code);
    }

    public UUID getId() {
        return id;
    }

    public UUID getParentInstitutionId() {
        return parentInstitutionId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void assignParent(UUID parentInstitutionId) {
        if (parentInstitutionId != null && parentInstitutionId.equals(this.id)) {
            throw new DomainException("An institution cannot be its own parent");
        }
        this.parentInstitutionId = parentInstitutionId;
        touch();
    }

    public void rename(String name, String code) {
        this.name = DomainAssertions.requireText(name, "name");
        this.code = DomainAssertions.normalizeCode(code);
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
