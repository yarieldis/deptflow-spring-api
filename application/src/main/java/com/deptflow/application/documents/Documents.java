package com.deptflow.application.documents;

import com.deptflow.application.exceptions.ForbiddenException;
import com.deptflow.application.exceptions.NotFoundException;
import com.deptflow.application.ports.DocumentRepository;
import com.deptflow.domain.Document;

import java.time.LocalDate;
import java.util.UUID;

/** Small shared helpers for the document use cases. */
final class Documents {

    private Documents() {
    }

    static Document loadTenantScoped(DocumentRepository documents, UUID id, UUID tenantId) {
        return documents.findById(id)
                .filter(d -> d.getInstitutionId().equals(tenantId))
                .orElseThrow(() -> new NotFoundException("document"));
    }

    static void requireMember(DocumentAccessPolicy policy, UUID personId, Document doc) {
        if (!policy.isActiveMember(personId, doc.getDepartmentId(), LocalDate.now())) {
            throw new ForbiddenException("Caller is not an active member of the document's department");
        }
    }
}
