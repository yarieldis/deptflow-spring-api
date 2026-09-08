package com.deptflow.application.ports;

import com.deptflow.domain.Institution;

import java.util.Optional;
import java.util.UUID;

public interface InstitutionRepository extends Repository<Institution, UUID> {

    Optional<Institution> findByCode(String code);
}
