package com.deptflow.infrastructure.persistence;

import com.deptflow.application.ports.DocumentTypeRepository;
import com.deptflow.domain.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaDocumentTypeRepository extends DocumentTypeRepository, JpaRepository<DocumentType, UUID> {
}
