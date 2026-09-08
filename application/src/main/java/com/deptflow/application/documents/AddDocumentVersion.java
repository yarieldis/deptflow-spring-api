package com.deptflow.application.documents;

import com.deptflow.application.common.CallerResolver;
import com.deptflow.application.ports.DocumentRepository;
import com.deptflow.application.ports.StorageService;
import com.deptflow.domain.Document;
import com.deptflow.domain.DocumentVersion;
import com.deptflow.domain.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Adds a new immutable version to a document (edit beside live). */
@Service
@Transactional
public class AddDocumentVersion {

    private final CallerResolver caller;
    private final DocumentRepository documents;
    private final DocumentAccessPolicy accessPolicy;
    private final StorageService storage;

    public AddDocumentVersion(CallerResolver caller, DocumentRepository documents,
                              DocumentAccessPolicy accessPolicy, StorageService storage) {
        this.caller = caller;
        this.documents = documents;
        this.accessPolicy = accessPolicy;
        this.storage = storage;
    }

    public DocumentDtos.DocumentVersionView execute(DocumentDtos.AddDocumentVersionCommand cmd) {
        UUID tenantId = caller.tenantId();
        Person person = caller.resolve();

        Document doc = Documents.loadTenantScoped(documents, cmd.documentId(), tenantId);
        Documents.requireMember(accessPolicy, person.getId(), doc);

        String storageKey = storage.save(cmd.content());
        try {
            DocumentVersion version = doc.addVersion(storageKey, null, cmd.fileName(), cmd.contentType(),
                    cmd.sizeBytes(), cmd.changeNote(), person.getId());
            documents.save(doc);
            return DocumentDtos.DocumentVersionView.from(version);
        } catch (RuntimeException e) {
            storage.delete(storageKey);
            throw e;
        }
    }
}
