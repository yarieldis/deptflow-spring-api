package com.deptflow.infrastructure.persistence;

import com.deptflow.application.ports.RoleRepository;
import com.deptflow.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaRoleRepository extends RoleRepository, JpaRepository<Role, UUID> {
}
