package com.deptflow.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * An organizational record of a person, optionally linked to a login account
 * via {@code userId}. People without a login account are representable.
 */
public class Person {

    private UUID id;
    private UUID institutionId;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String jobTitle;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    /** For persistence tooling. */
    protected Person() {
    }

    private Person(UUID id, UUID institutionId, String firstName, String lastName) {
        this.id = id;
        this.institutionId = DomainAssertions.requireId(institutionId, "institutionId");
        this.firstName = DomainAssertions.requireText(firstName, "firstName");
        this.lastName = DomainAssertions.requireText(lastName, "lastName");
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Person create(UUID institutionId, String firstName, String lastName) {
        return new Person(UUID.randomUUID(), institutionId, firstName, lastName);
    }

    public UUID getId() {
        return id;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getJobTitle() {
        return jobTitle;
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

    public void linkLogin(UUID userId) {
        this.userId = DomainAssertions.requireId(userId, "userId");
        touch();
    }

    public void unlinkLogin() {
        this.userId = null;
        touch();
    }

    public void rename(String firstName, String lastName) {
        this.firstName = DomainAssertions.requireText(firstName, "firstName");
        this.lastName = DomainAssertions.requireText(lastName, "lastName");
        touch();
    }

    public void updateContactInfo(String email, String phone, String jobTitle) {
        this.email = email;
        this.phone = phone;
        this.jobTitle = jobTitle;
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
