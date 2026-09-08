package com.deptflow.api.security;

import com.deptflow.application.ports.CurrentTenantProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Reads the current institution id from the JWT {@code institution_id} claim. */
@Component
public class SecurityContextCurrentTenantProvider implements CurrentTenantProvider {

    @Override
    public Optional<UUID> currentInstitutionId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }
        String claim = jwt.getClaimAsString("institution_id");
        if (claim == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(claim));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
