package com.deptflow.application.ports;

import com.deptflow.domain.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends Repository<Role, UUID> {

    List<Role> findByInstitutionId(UUID institutionId);

    Optional<Role> findByInstitutionIdAndName(UUID institutionId, String name);
}
