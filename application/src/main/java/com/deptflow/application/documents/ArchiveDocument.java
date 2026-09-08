package com.deptflow.application.documents;

import com.deptflow.application.common.CallerResolver;
import com.deptflow.application.ports.DocumentRepository;
import com.deptflow.domain.Document;
import com.deptflow.domain.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Archives a document that has at least one Approved version. */
@Service
@Transactional
public class ArchiveDocument {

    private final CallerResolver caller;
    private final DocumentRepository documents;
    private final DocumentAccessPolicy accessPolicy;

    public ArchiveDocument(CallerResolver caller, DocumentRepository documents, DocumentAccessPolicy accessPolicy) {
        this.caller = caller;
        this.documents = documents;
        this.accessPolicy = accessPolicy;
    }

    public DocumentDtos.DocumentView execute(DocumentDtos.ArchiveCommand cmd) {
        UUID tenantId = caller.tenantId();
        Person person = caller.resolve();

        Document doc = Documents.loadTenantScoped(documents, cmd.documentId(), tenantId);
        Documents.requireMember(accessPolicy, person.getId(), doc);

        doc.archive(person.getId());
        return DocumentDtos.DocumentView.from(documents.save(doc));
    }
}
