package com.deptflow.application.documents;

import com.deptflow.application.common.CallerResolver;
import com.deptflow.application.exceptions.ForbiddenException;
import com.deptflow.application.ports.DocumentRepository;
import com.deptflow.domain.Document;
import com.deptflow.domain.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/** Approves the latest pending version of a document (approver role required). */
@Service
@Transactional
public class ApproveDocument {

    private final CallerResolver caller;
    private final DocumentRepository documents;
    private final DocumentAccessPolicy accessPolicy;

    public ApproveDocument(CallerResolver caller, DocumentRepository documents, DocumentAccessPolicy accessPolicy) {
        this.caller = caller;
        this.documents = documents;
        this.accessPolicy = accessPolicy;
    }

    public DocumentDtos.DocumentView execute(DocumentDtos.ApproveCommand cmd) {
        UUID tenantId = caller.tenantId();
        Person person = caller.resolve();

        Document doc = Documents.loadTenantScoped(documents, cmd.documentId(), tenantId);
        if (!accessPolicy.canApprove(person.getId(), doc.getDepartmentId(), LocalDate.now())) {
            throw new ForbiddenException("Caller cannot approve documents in this department");
        }

        doc.approve(person.getId());
        return DocumentDtos.DocumentView.from(documents.save(doc));
    }
}
