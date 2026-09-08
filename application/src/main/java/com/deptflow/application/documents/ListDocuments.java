package com.deptflow.application.documents;

import com.deptflow.application.common.CallerResolver;
import com.deptflow.application.exceptions.ForbiddenException;
import com.deptflow.application.exceptions.NotFoundException;
import com.deptflow.application.ports.DepartmentRepository;
import com.deptflow.application.ports.DocumentRepository;
import com.deptflow.domain.Department;
import com.deptflow.domain.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Lists tenant-scoped documents in a department with access enforced. */
@Service
@Transactional(readOnly = true)
public class ListDocuments {

    private final CallerResolver caller;
    private final DepartmentRepository departments;
    private final DocumentRepository documents;
    private final DocumentAccessPolicy accessPolicy;

    public ListDocuments(CallerResolver caller, DepartmentRepository departments,
                         DocumentRepository documents, DocumentAccessPolicy accessPolicy) {
        this.caller = caller;
        this.departments = departments;
        this.documents = documents;
        this.accessPolicy = accessPolicy;
    }

    public List<DocumentDtos.DocumentView> execute(UUID departmentId) {
        UUID tenantId = caller.tenantId();
        Person person = caller.resolve();

        Department department = departments.findById(departmentId)
                .filter(d -> d.getInstitutionId().equals(tenantId))
                .orElseThrow(() -> new NotFoundException("department"));
        if (!accessPolicy.isActiveMember(person.getId(), department.getId(), LocalDate.now())) {
            throw new ForbiddenException("Caller is not an active member of the department");
        }

        return documents.findByDepartmentId(departmentId).stream()
                .filter(d -> d.getInstitutionId().equals(tenantId))
                .map(DocumentDtos.DocumentView::from)
                .toList();
    }
}
