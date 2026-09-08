package com.deptflow.application.ports;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides the institution id of the current tenant. Implemented in
 * Infrastructure from the authenticated request context; the Application layer
 * depends only on this port.
 */
public interface CurrentTenantProvider {

    Optional<UUID> currentInstitutionId();
}
