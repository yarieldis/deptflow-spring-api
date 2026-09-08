package com.deptflow.application.ports;

import com.deptflow.domain.Document;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends Repository<Document, UUID> {

    List<Document> findByDepartmentId(UUID departmentId);

    List<Document> findByInstitutionId(UUID institutionId);
}
