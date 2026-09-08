package com.deptflow.application.people;

import com.deptflow.application.common.CallerResolver;
import com.deptflow.application.exceptions.NotFoundException;
import com.deptflow.application.ports.DepartmentRepository;
import com.deptflow.application.ports.PersonDepartmentRepository;
import com.deptflow.application.ports.PersonRepository;
import com.deptflow.application.ports.RoleRepository;
import com.deptflow.domain.Department;
import com.deptflow.domain.Person;
import com.deptflow.domain.PersonDepartment;
import com.deptflow.domain.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Assigns a person to a department with a role and start date, tenant-scoped. */
@Service
@Transactional
public class AssignMembership {

    private final CallerResolver caller;
    private final PersonRepository persons;
    private final DepartmentRepository departments;
    private final RoleRepository roles;
    private final PersonDepartmentRepository memberships;

    public AssignMembership(CallerResolver caller, PersonRepository persons, DepartmentRepository departments,
                            RoleRepository roles, PersonDepartmentRepository memberships) {
        this.caller = caller;
        this.persons = persons;
        this.departments = departments;
        this.roles = roles;
        this.memberships = memberships;
    }

    public PeopleDtos.MembershipView execute(PeopleDtos.AssignMembershipCommand cmd) {
        UUID tenantId = caller.tenantId();

        Person person = persons.findById(cmd.personId())
                .filter(p -> p.getInstitutionId().equals(tenantId))
                .orElseThrow(() -> new NotFoundException("person"));
        Department department = departments.findById(cmd.departmentId())
                .filter(d -> d.getInstitutionId().equals(tenantId))
                .orElseThrow(() -> new NotFoundException("department"));
        Role role = roles.findById(cmd.roleId())
                .filter(r -> r.getInstitutionId().equals(tenantId))
                .orElseThrow(() -> new NotFoundException("role"));

        PersonDepartment membership = PersonDepartment.create(person.getId(), department.getId(),
                role.getId(), cmd.primary(), cmd.startDate());
        return PeopleDtos.MembershipView.from(memberships.save(membership));
    }
}
