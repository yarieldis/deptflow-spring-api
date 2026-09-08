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

/** Rejects the latest pending version of a document with a required comment. */
@Service
@Transactional
public class RejectDocument {

    private final CallerResolver caller;
    private final DocumentRepository documents;
    private final DocumentAccessPolicy accessPolicy;

    public RejectDocument(CallerResolver caller, DocumentRepository documents, DocumentAccessPolicy accessPolicy) {
        this.caller = caller;
        this.documents = documents;
        this.accessPolicy = accessPolicy;
    }

    public DocumentDtos.DocumentView execute(DocumentDtos.RejectCommand cmd) {
        UUID tenantId = caller.tenantId();
        Person person = caller.resolve();

        Document doc = Documents.loadTenantScoped(documents, cmd.documentId(), tenantId);
        if (!accessPolicy.canApprove(person.getId(), doc.getDepartmentId(), LocalDate.now())) {
            throw new ForbiddenException("Caller cannot reject documents in this department");
        }

        doc.reject(person.getId(), cmd.comment());
        return DocumentDtos.DocumentView.from(documents.save(doc));
    }
}
