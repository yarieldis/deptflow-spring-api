package com.deptflow.application.documents;

import com.deptflow.application.common.CallerResolver;
import com.deptflow.application.exceptions.ForbiddenException;
import com.deptflow.application.exceptions.NotFoundException;
import com.deptflow.application.ports.DepartmentRepository;
import com.deptflow.application.ports.DocumentRepository;
import com.deptflow.application.ports.StorageService;
import com.deptflow.domain.Department;
import com.deptflow.domain.Document;
import com.deptflow.domain.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Creates a document with its first version. Files are written first, rows
 * second, with best-effort compensation if the persist step fails (a rare
 * orphaned file is acceptable).
 */
@Service
@Transactional
public class CreateDocument {

    private final CallerResolver caller;
    private final DepartmentRepository departments;
    private final DocumentRepository documents;
    private final DocumentAccessPolicy accessPolicy;
    private final StorageService storage;

    public CreateDocument(CallerResolver caller, DepartmentRepository departments, DocumentRepository documents,
                          DocumentAccessPolicy accessPolicy, StorageService storage) {
        this.caller = caller;
        this.departments = departments;
        this.documents = documents;
        this.accessPolicy = accessPolicy;
        this.storage = storage;
    }

    public DocumentDtos.DocumentView execute(DocumentDtos.CreateDocumentCommand cmd) {
        UUID tenantId = caller.tenantId();
        Person person = caller.resolve();

        Department department = departments.findById(cmd.departmentId())
                .filter(d -> d.getInstitutionId().equals(tenantId))
                .orElseThrow(() -> new NotFoundException("department"));
        if (!accessPolicy.isActiveMember(person.getId(), department.getId(), LocalDate.now())) {
            throw new ForbiddenException("Caller is not an active member of the department");
        }

        String storageKey = storage.save(cmd.content());
        try {
            Document doc = Document.create(tenantId, department.getId(), cmd.documentTypeId(),
                    cmd.title(), cmd.description(), person.getId());
            doc.addVersion(storageKey, null, cmd.fileName(), cmd.contentType(), cmd.sizeBytes(),
                    cmd.changeNote(), person.getId());
            return DocumentDtos.DocumentView.from(documents.save(doc));
        } catch (RuntimeException e) {
            storage.delete(storageKey);
            throw e;
        }
    }
}
