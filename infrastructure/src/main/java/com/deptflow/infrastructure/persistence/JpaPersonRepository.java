package com.deptflow.infrastructure.persistence;

import com.deptflow.application.ports.PersonRepository;
import com.deptflow.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPersonRepository extends PersonRepository, JpaRepository<Person, UUID> {
}
