package com.deptflow.application.ports;

import com.deptflow.domain.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends Repository<Department, UUID> {

    List<Department> findByInstitutionId(UUID institutionId);

    Optional<Department> findByInstitutionIdAndCode(UUID institutionId, String code);
}
