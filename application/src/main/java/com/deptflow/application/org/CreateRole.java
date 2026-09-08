package com.deptflow.application.org;

import com.deptflow.application.common.CallerResolver;
import com.deptflow.application.exceptions.ValidationException;
import com.deptflow.application.ports.RoleRepository;
import com.deptflow.domain.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Creates a role in the current institution (tenant-scoped). */
@Service
@Transactional
public class CreateRole {

    private final CallerResolver caller;
    private final RoleRepository roles;

    public CreateRole(CallerResolver caller, RoleRepository roles) {
        this.caller = caller;
        this.roles = roles;
    }

    public OrgDtos.RoleView execute(OrgDtos.CreateRoleCommand cmd) {
        UUID tenantId = caller.tenantId();

        if (cmd.name() != null && !cmd.name().isBlank()
                && roles.findByInstitutionIdAndName(tenantId, cmd.name().trim()).isPresent()) {
            throw new ValidationException("Role name already exists in the institution");
        }

        Role role = Role.create(tenantId, cmd.name(), cmd.description());
        role.setCanApproveDocuments(cmd.canApproveDocuments());
        return OrgDtos.RoleView.from(roles.save(role));
    }
}
