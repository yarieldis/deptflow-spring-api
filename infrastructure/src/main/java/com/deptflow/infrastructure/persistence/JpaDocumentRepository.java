package com.deptflow.infrastructure.persistence;

import com.deptflow.application.ports.DocumentRepository;
import com.deptflow.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaDocumentRepository extends DocumentRepository, JpaRepository<Document, UUID> {
}
