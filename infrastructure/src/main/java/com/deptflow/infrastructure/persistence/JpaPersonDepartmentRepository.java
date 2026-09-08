package com.deptflow.infrastructure.persistence;

import com.deptflow.application.ports.PersonDepartmentRepository;
import com.deptflow.domain.PersonDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPersonDepartmentRepository extends PersonDepartmentRepository, JpaRepository<PersonDepartment, UUID> {
}
