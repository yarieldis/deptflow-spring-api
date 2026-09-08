package com.deptflow.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Membership of a person in a department, carrying a role, a primary flag, and
 * an active date range. Re-joining a department creates a new membership period
 * without overwriting history.
 */
public class PersonDepartment {

    private UUID id;
    private UUID personId;
    private UUID departmentId;
    private UUID roleId;
    private boolean primary;
    private LocalDate startDate;
    private LocalDate endDate;

    /** For persistence tooling. */
    protected PersonDepartment() {
    }

    private PersonDepartment(UUID id, UUID personId, UUID departmentId, UUID roleId, boolean primary, LocalDate startDate) {
        this.id = id;
        this.personId = DomainAssertions.requireId(personId, "personId");
        this.departmentId = DomainAssertions.requireId(departmentId, "departmentId");
        this.roleId = DomainAssertions.requireId(roleId, "roleId");
        this.primary = primary;
        this.startDate = DomainAssertions.requireDate(startDate, "startDate");
    }

    public static PersonDepartment create(UUID personId, UUID departmentId, UUID roleId, boolean primary, LocalDate startDate) {
        return new PersonDepartment(UUID.randomUUID(), personId, departmentId, roleId, primary, startDate);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPersonId() {
        return personId;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public boolean isPrimary() {
        return primary;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * A membership is active when {@code startDate <= date} and either there is no
     * end date or {@code date <= endDate} (a future end date is still active).
     */
    public boolean isActiveOn(LocalDate date) {
        if (date == null) {
            throw new DomainException("date is required");
        }
        return !startDate.isAfter(date) && (endDate == null || !date.isAfter(endDate));
    }

    public void end(LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new DomainException("Membership end date cannot precede its start date");
        }
        this.endDate = endDate;
    }
}
