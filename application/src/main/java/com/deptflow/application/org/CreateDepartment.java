package com.deptflow.application.org;

import com.deptflow.application.common.CallerResolver;
import com.deptflow.application.exceptions.ValidationException;
import com.deptflow.application.ports.DepartmentRepository;
import com.deptflow.domain.Codes;
import com.deptflow.domain.Department;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Creates a department, tenant-stamped with the current institution. */
@Service
@Transactional
public class CreateDepartment {

    private final CallerResolver caller;
    private final DepartmentRepository departments;

    public CreateDepartment(CallerResolver caller, DepartmentRepository departments) {
        this.caller = caller;
        this.departments = departments;
    }

    public OrgDtos.DepartmentView execute(OrgDtos.CreateDepartmentCommand cmd) {
        UUID tenantId = caller.tenantId();

        if (cmd.parentDepartmentId() != null) {
            departments.findById(cmd.parentDepartmentId())
                    .filter(d -> d.getInstitutionId().equals(tenantId))
                    .orElseThrow(() -> new ValidationException("Parent department must belong to the same institution"));
        }
        if (departments.findByInstitutionIdAndCode(tenantId, Codes.normalize(cmd.code())).isPresent()) {
            throw new ValidationException("Department code already exists in the institution");
        }

        Department department = Department.create(tenantId, cmd.parentDepartmentId(), cmd.name(), cmd.code());
        return OrgDtos.DepartmentView.from(departments.save(department));
    }
}
