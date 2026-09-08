package com.deptflow.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * A global login account (the Java replacement for ASP.NET Core Identity's
 * {@code IdentityUser}). Accounts are not tenant-scoped; a {@link Person} is
 * linked to an account through its nullable {@code userId}.
 */
public class UserAccount {

    private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    /** For persistence tooling. */
    protected UserAccount() {
    }

    private UserAccount(UUID id, String username, String email, String passwordHash) {
        this.id = id;
        this.username = DomainAssertions.requireText(username, "username");
        this.email = DomainAssertions.requireText(email, "email");
        this.passwordHash = DomainAssertions.requireText(passwordHash, "passwordHash");
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static UserAccount create(String username, String email, String passwordHash) {
        return new UserAccount(UUID.randomUUID(), username, email, passwordHash);
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
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

    public void changePassword(String passwordHash) {
        this.passwordHash = DomainAssertions.requireText(passwordHash, "passwordHash");
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
