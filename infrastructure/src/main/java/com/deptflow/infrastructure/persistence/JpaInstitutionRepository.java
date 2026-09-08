package com.deptflow.infrastructure.persistence;

import com.deptflow.application.ports.InstitutionRepository;
import com.deptflow.domain.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaInstitutionRepository extends InstitutionRepository, JpaRepository<Institution, UUID> {
}
