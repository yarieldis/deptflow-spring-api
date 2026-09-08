package com.deptflow.domain;

import java.util.function.Function;

/**
 * Detects cycles in self-referencing hierarchies (institutions and departments).
 */
public final class CycleDetector {

    private CycleDetector() {
    }

    /**
     * Returns {@code true} if assigning {@code candidateParentId} as the parent of
     * {@code entityId} would make {@code entityId} its own ancestor.
     *
     * @param entityId          the entity being reparented
     * @param candidateParentId the proposed parent (may be {@code null})
     * @param parentIdResolver  returns the current parent id of a given entity id, or {@code null}
     */
    public static <T> boolean wouldCreateCycle(T entityId, T candidateParentId, Function<T, T> parentIdResolver) {
        if (candidateParentId == null) {
            return false;
        }
        if (entityId.equals(candidateParentId)) {
            return true;
        }
        T cursor = parentIdResolver.apply(candidateParentId);
        while (cursor != null) {
            if (cursor.equals(entityId)) {
                return true;
            }
            cursor = parentIdResolver.apply(cursor);
        }
        return false;
    }
}
