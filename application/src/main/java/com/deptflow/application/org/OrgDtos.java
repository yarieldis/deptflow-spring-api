package com.deptflow.application.org;

import com.deptflow.domain.Department;
import com.deptflow.domain.Institution;
import com.deptflow.domain.Role;

import java.util.UUID;

/** Request/response contracts for institution, department, and role use cases. */
public final class OrgDtos {

    private OrgDtos() {
    }

    public record CreateInstitutionCommand(UUID parentInstitutionId, String name, String code) {
    }

    public record InstitutionView(UUID id, UUID parentInstitutionId, String name, String code, boolean active) {

        public static InstitutionView from(Institution i) {
            return new InstitutionView(i.getId(), i.getParentInstitutionId(), i.getName(), i.getCode(), i.isActive());
        }
    }

    public record CreateDepartmentCommand(UUID parentDepartmentId, String name, String code) {
    }

    public record DepartmentView(
            UUID id,
            UUID institutionId,
            UUID parentDepartmentId,
            UUID headPersonId,
            String name,
            String code,
            boolean active) {

        public static DepartmentView from(Department d) {
            return new DepartmentView(d.getId(), d.getInstitutionId(), d.getParentDepartmentId(),
                    d.getHeadPersonId(), d.getName(), d.getCode(), d.isActive());
        }
    }

    public record CreateRoleCommand(String name, String description, boolean canApproveDocuments) {
    }

    public record RoleView(
            UUID id,
            UUID institutionId,
            String name,
            String description,
            boolean active,
            boolean canApproveDocuments) {

        public static RoleView from(Role r) {
            return new RoleView(r.getId(), r.getInstitutionId(), r.getName(), r.getDescription(),
                    r.isActive(), r.isCanApproveDocuments());
        }
    }
}
