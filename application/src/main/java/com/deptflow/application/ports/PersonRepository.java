package com.deptflow.application.ports;

import com.deptflow.domain.Person;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends Repository<Person, UUID> {

    List<Person> findByInstitutionId(UUID institutionId);

    List<Person> findByUserId(UUID userId);

    Optional<Person> findByUserIdAndInstitutionId(UUID userId, UUID institutionId);
}
