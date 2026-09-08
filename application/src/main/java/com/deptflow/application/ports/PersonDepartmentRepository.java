package com.deptflow.application.ports;

import com.deptflow.domain.PersonDepartment;

import java.util.List;
import java.util.UUID;

public interface PersonDepartmentRepository extends Repository<PersonDepartment, UUID> {

    List<PersonDepartment> findByPersonId(UUID personId);

    List<PersonDepartment> findByDepartmentId(UUID departmentId);

    List<PersonDepartment> findByPersonIdAndDepartmentId(UUID personId, UUID departmentId);
}
