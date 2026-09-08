package com.deptflow.application.common;

import com.deptflow.application.exceptions.ForbiddenException;
import com.deptflow.application.exceptions.TenantRequiredException;
import com.deptflow.application.ports.CurrentTenantProvider;
import com.deptflow.application.ports.CurrentUserProvider;
import com.deptflow.application.ports.PersonRepository;
import com.deptflow.domain.Person;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Resolves the authenticated login to a tenant-scoped {@link Person} (a login
 * can map to many persons across tenants). Use cases read the tenant id and
 * caller through this helper.
 */
@Service
public class CallerResolver {

    private final CurrentTenantProvider tenants;
    private final CurrentUserProvider users;
    private final PersonRepository persons;

    public CallerResolver(CurrentTenantProvider tenants, CurrentUserProvider users, PersonRepository persons) {
        this.tenants = tenants;
        this.users = users;
        this.persons = persons;
    }

    public UUID tenantId() {
        return tenants.currentInstitutionId().orElseThrow(TenantRequiredException::new);
    }

    public Person resolve() {
        UUID tenantId = tenantId();
        UUID userId = users.currentUserId()
                .orElseThrow(() -> new ForbiddenException("An authenticated user is required"));
        return persons.findByUserIdAndInstitutionId(userId, tenantId)
                .orElseThrow(() -> new ForbiddenException("No person record for the current user in this tenant"));
    }
}
