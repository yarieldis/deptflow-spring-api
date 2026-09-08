package com.deptflow.application.ports;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides the id of the authenticated login account. Implemented in
 * Infrastructure from the security context; the Application layer depends only
 * on this port.
 */
public interface CurrentUserProvider {

    Optional<UUID> currentUserId();
}
