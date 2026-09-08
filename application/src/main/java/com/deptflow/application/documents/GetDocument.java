package com.deptflow.application.documents;

import com.deptflow.application.common.CallerResolver;
import com.deptflow.application.ports.DocumentRepository;
import com.deptflow.domain.Document;
import com.deptflow.domain.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Returns a single tenant-scoped document with department access enforced. */
@Service
@Transactional(readOnly = true)
public class GetDocument {

    private final CallerResolver caller;
    private final DocumentRepository documents;
    private final DocumentAccessPolicy accessPolicy;

    public GetDocument(CallerResolver caller, DocumentRepository documents, DocumentAccessPolicy accessPolicy) {
        this.caller = caller;
        this.documents = documents;
        this.accessPolicy = accessPolicy;
    }

    public DocumentDtos.DocumentView execute(UUID documentId) {
        UUID tenantId = caller.tenantId();
        Person person = caller.resolve();

        Document doc = Documents.loadTenantScoped(documents, documentId, tenantId);
        Documents.requireMember(accessPolicy, person.getId(), doc);
        return DocumentDtos.DocumentView.from(doc);
    }
}
