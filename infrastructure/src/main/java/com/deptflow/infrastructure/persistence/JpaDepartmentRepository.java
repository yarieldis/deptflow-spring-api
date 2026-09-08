package com.deptflow.infrastructure.persistence;

import com.deptflow.application.ports.DepartmentRepository;
import com.deptflow.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaDepartmentRepository extends DepartmentRepository, JpaRepository<Department, UUID> {
}
