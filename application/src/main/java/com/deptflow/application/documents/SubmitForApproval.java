package com.deptflow.application.documents;

import com.deptflow.application.common.CallerResolver;
import com.deptflow.application.ports.DocumentRepository;
import com.deptflow.domain.Document;
import com.deptflow.domain.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Submits the latest version of a document for approval. */
@Service
@Transactional
public class SubmitForApproval {

    private final CallerResolver caller;
    private final DocumentRepository documents;
    private final DocumentAccessPolicy accessPolicy;

    public SubmitForApproval(CallerResolver caller, DocumentRepository documents, DocumentAccessPolicy accessPolicy) {
        this.caller = caller;
        this.documents = documents;
        this.accessPolicy = accessPolicy;
    }

    public DocumentDtos.DocumentView execute(DocumentDtos.SubmitCommand cmd) {
        UUID tenantId = caller.tenantId();
        Person person = caller.resolve();

        Document doc = Documents.loadTenantScoped(documents, cmd.documentId(), tenantId);
        Documents.requireMember(accessPolicy, person.getId(), doc);

        doc.submit(person.getId());
        return DocumentDtos.DocumentView.from(documents.save(doc));
    }
}
