package com.deptflow.application.ports;

import com.deptflow.domain.DocumentType;

import java.util.List;
import java.util.UUID;

public interface DocumentTypeRepository extends Repository<DocumentType, UUID> {

    List<DocumentType> findByInstitutionId(UUID institutionId);
}
