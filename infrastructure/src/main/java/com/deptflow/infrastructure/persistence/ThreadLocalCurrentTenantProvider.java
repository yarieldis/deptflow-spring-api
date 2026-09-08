package com.deptflow.infrastructure.persistence;

import com.deptflow.application.ports.CurrentTenantProvider;

import java.util.Optional;
import java.util.UUID;

/**
 * Thread-local implementation of {@link CurrentTenantProvider}. In production
 * this will be replaced by a security-context-backed implementation; the
 * static setters exist to drive tenant context in tests and early wiring.
 */
public class ThreadLocalCurrentTenantProvider implements CurrentTenantProvider {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    public static void setCurrent(UUID institutionId) {
        CURRENT.set(institutionId);
    }

    public static void clear() {
        CURRENT.remove();
    }

    @Override
    public Optional<UUID> currentInstitutionId() {
        return Optional.ofNullable(CURRENT.get());
    }
}
