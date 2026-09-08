package com.deptflow.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * A department owned by exactly one institution, with optional nesting and an
 * optional head person.
 */
public class Department {

    private UUID id;
    private UUID institutionId;
    private UUID parentDepartmentId;
    private UUID headPersonId;
    private String name;
    private String code;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    /** For persistence tooling. */
    protected Department() {
    }

    private Department(UUID id, UUID institutionId, UUID parentDepartmentId, String name, String code) {
        this.id = id;
        this.institutionId = DomainAssertions.requireId(institutionId, "institutionId");
        this.parentDepartmentId = parentDepartmentId;
        this.name = DomainAssertions.requireText(name, "name");
        this.code = DomainAssertions.normalizeCode(code);
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Department create(UUID institutionId, String name, String code) {
        return create(institutionId, null, name, code);
    }

    public static Department create(UUID institutionId, UUID parentDepartmentId, String name, String code) {
        return new Department(UUID.randomUUID(), institutionId, parentDepartmentId, name, code);
    }

    public UUID getId() {
        return id;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public UUID getParentDepartmentId() {
        return parentDepartmentId;
    }

    public UUID getHeadPersonId() {
        return headPersonId;
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

    public void assignParent(UUID parentDepartmentId) {
        if (parentDepartmentId != null && parentDepartmentId.equals(this.id)) {
            throw new DomainException("A department cannot be its own parent");
        }
        this.parentDepartmentId = parentDepartmentId;
        touch();
    }

    public void assignHead(UUID headPersonId) {
        this.headPersonId = headPersonId;
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
