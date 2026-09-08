package com.deptflow.application.documents;

import com.deptflow.application.ports.PersonDepartmentRepository;
import com.deptflow.application.ports.RoleRepository;
import com.deptflow.domain.PersonDepartment;
import com.deptflow.domain.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentAccessPolicyTest {

    private final PersonDepartmentRepository memberships = mock(PersonDepartmentRepository.class);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final DocumentAccessPolicy policy = new DocumentAccessPolicy(memberships, roles);

    private final UUID institutionId = UUID.randomUUID();
    private final UUID personId = UUID.randomUUID();
    private final UUID departmentId = UUID.randomUUID();
    private final UUID roleId = UUID.randomUUID();
    private final LocalDate today = LocalDate.now();

    @Test
    void activeMembershipGrantsAccess() {
        PersonDepartment active = PersonDepartment.create(personId, departmentId, roleId, true, today.minusDays(1));
        when(memberships.findByPersonIdAndDepartmentId(personId, departmentId)).thenReturn(List.of(active));

        assertThat(policy.isActiveMember(personId, departmentId, today)).isTrue();
    }

    @Test
    void endedMembershipDeniesAccess() {
        PersonDepartment ended = PersonDepartment.create(personId, departmentId, roleId, true, today.minusDays(10));
        ended.end(today.minusDays(1));
        when(memberships.findByPersonIdAndDepartmentId(personId, departmentId)).thenReturn(List.of(ended));

        assertThat(policy.isActiveMember(personId, departmentId, today)).isFalse();
    }

    @Test
    void futureEndDateIsStillActive() {
        PersonDepartment membership = PersonDepartment.create(personId, departmentId, roleId, true, today.minusDays(1));
        membership.end(today.plusDays(5));
        when(memberships.findByPersonIdAndDepartmentId(personId, departmentId)).thenReturn(List.of(membership));

        assertThat(policy.isActiveMember(personId, departmentId, today)).isTrue();
    }

    @Test
    void approverRoleGrantsApproval() {
        PersonDepartment active = PersonDepartment.create(personId, departmentId, roleId, true, today.minusDays(1));
        Role approver = Role.create(institutionId, "Approver", null);
        approver.setCanApproveDocuments(true);
        when(memberships.findByPersonIdAndDepartmentId(personId, departmentId)).thenReturn(List.of(active));
        when(roles.findById(roleId)).thenReturn(Optional.of(approver));

        assertThat(policy.canApprove(personId, departmentId, today)).isTrue();
    }

    @Test
    void nonApproverRoleDeniesApproval() {
        PersonDepartment active = PersonDepartment.create(personId, departmentId, roleId, true, today.minusDays(1));
        Role member = Role.create(institutionId, "Member", null);
        when(memberships.findByPersonIdAndDepartmentId(personId, departmentId)).thenReturn(List.of(active));
        when(roles.findById(roleId)).thenReturn(Optional.of(member));

        assertThat(policy.canApprove(personId, departmentId, today)).isFalse();
    }
}
