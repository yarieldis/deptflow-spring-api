package com.deptflow.application.documents;

import com.deptflow.application.ports.PersonDepartmentRepository;
import com.deptflow.application.ports.RoleRepository;
import com.deptflow.domain.PersonDepartment;
import com.deptflow.domain.Role;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Department-scoped document access, derived from active membership in the
 * document's owning department. Approval additionally requires the membership's
 * role to carry {@code canApproveDocuments}.
 */
@Service
public class DocumentAccessPolicy {

    private final PersonDepartmentRepository memberships;
    private final RoleRepository roles;

    public DocumentAccessPolicy(PersonDepartmentRepository memberships, RoleRepository roles) {
        this.memberships = memberships;
        this.roles = roles;
    }

    public boolean isActiveMember(UUID personId, UUID departmentId, LocalDate on) {
        return memberships.findByPersonIdAndDepartmentId(personId, departmentId).stream()
                .anyMatch(m -> m.isActiveOn(on));
    }

    public boolean canApprove(UUID personId, UUID departmentId, LocalDate on) {
        return memberships.findByPersonIdAndDepartmentId(personId, departmentId).stream()
                .filter(m -> m.isActiveOn(on))
                .map(PersonDepartment::getRoleId)
                .map(roles::findById)
                .flatMap(Optional::stream)
                .anyMatch(Role::isCanApproveDocuments);
    }
}
