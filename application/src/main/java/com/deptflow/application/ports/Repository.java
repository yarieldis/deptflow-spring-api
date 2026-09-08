package com.deptflow.application.ports;

import java.util.Optional;

/**
 * Minimal persistence port shared by all typed repositories. Tenant-owned
 * entities add institution-scoped query methods on their typed interface.
 */
public interface Repository<T, ID> {

    Optional<T> findById(ID id);

    T save(T entity);

    void deleteById(ID id);
}
