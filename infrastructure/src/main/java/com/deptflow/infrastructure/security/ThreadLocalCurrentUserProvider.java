package com.deptflow.infrastructure.security;

import com.deptflow.application.ports.CurrentUserProvider;

import java.util.Optional;
import java.util.UUID;

/**
 * Thread-local implementation of {@link CurrentUserProvider}. In production
 * this is replaced by a security-context-backed implementation; the static
 * setters drive the user context in tests and early wiring.
 */
public class ThreadLocalCurrentUserProvider implements CurrentUserProvider {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    public static void setCurrent(UUID userId) {
        CURRENT.set(userId);
    }

    public static void clear() {
        CURRENT.remove();
    }

    @Override
    public Optional<UUID> currentUserId() {
        return Optional.ofNullable(CURRENT.get());
    }
}
