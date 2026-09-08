package com.deptflow.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PersonDepartmentTest {

    private final UUID personId = UUID.randomUUID();
    private final UUID departmentId = UUID.randomUUID();
    private final UUID roleId = UUID.randomUUID();
    private final LocalDate today = LocalDate.of(2026, 9, 8);

    @Test
    void activeWhenStartedAndNoEndDate() {
        PersonDepartment m = PersonDepartment.create(personId, departmentId, roleId, true, today.minusDays(1));
        assertThat(m.isActiveOn(today)).isTrue();
    }

    @Test
    void activeWhenEndDateIsInTheFuture() {
        PersonDepartment m = PersonDepartment.create(personId, departmentId, roleId, true, today.minusDays(1));
        m.end(today.plusDays(5));
        assertThat(m.isActiveOn(today)).isTrue();
    }

    @Test
    void inactiveWhenEndDateHasPassed() {
        PersonDepartment m = PersonDepartment.create(personId, departmentId, roleId, true, today.minusDays(10));
        m.end(today.minusDays(1));
        assertThat(m.isActiveOn(today)).isFalse();
    }

    @Test
    void inactiveBeforeStartDate() {
        PersonDepartment m = PersonDepartment.create(personId, departmentId, roleId, true, today.plusDays(1));
        assertThat(m.isActiveOn(today)).isFalse();
    }
}
