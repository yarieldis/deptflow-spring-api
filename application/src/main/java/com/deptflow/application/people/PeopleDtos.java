package com.deptflow.application.people;

import com.deptflow.domain.Person;
import com.deptflow.domain.PersonDepartment;

import java.time.LocalDate;
import java.util.UUID;

/** Request/response contracts for the people and membership use cases. */
public final class PeopleDtos {

    private PeopleDtos() {
    }

    public record CreatePersonCommand(String firstName, String lastName, String email, String phone, String jobTitle) {
    }

    public record PersonView(
            UUID id,
            UUID institutionId,
            UUID userId,
            String firstName,
            String lastName,
            String email,
            String phone,
            String jobTitle,
            boolean active) {

        public static PersonView from(Person p) {
            return new PersonView(p.getId(), p.getInstitutionId(), p.getUserId(), p.getFirstName(),
                    p.getLastName(), p.getEmail(), p.getPhone(), p.getJobTitle(), p.isActive());
        }
    }

    public record AssignMembershipCommand(UUID personId, UUID departmentId, UUID roleId, boolean primary, LocalDate startDate) {
    }

    public record MembershipView(
            UUID id,
            UUID personId,
            UUID departmentId,
            UUID roleId,
            boolean primary,
            LocalDate startDate,
            LocalDate endDate) {

        public static MembershipView from(PersonDepartment m) {
            return new MembershipView(m.getId(), m.getPersonId(), m.getDepartmentId(), m.getRoleId(),
                    m.isPrimary(), m.getStartDate(), m.getEndDate());
        }
    }
}
