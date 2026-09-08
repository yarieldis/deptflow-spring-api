package com.deptflow.api.web;

import com.deptflow.application.documents.DocumentDtos;
import com.deptflow.application.documents.ListDocuments;
import com.deptflow.application.org.CreateDepartment;
import com.deptflow.application.org.OrgDtos;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final CreateDepartment createDepartment;
    private final ListDocuments listDocuments;

    public DepartmentController(CreateDepartment createDepartment, ListDocuments listDocuments) {
        this.createDepartment = createDepartment;
        this.listDocuments = listDocuments;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrgDtos.DepartmentView create(@RequestBody OrgDtos.CreateDepartmentCommand command) {
        return createDepartment.execute(command);
    }

    @GetMapping("/{id}/documents")
    public List<DocumentDtos.DocumentView> documents(@PathVariable UUID id) {
        return listDocuments.execute(id);
    }
}
