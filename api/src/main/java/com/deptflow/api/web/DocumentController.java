package com.deptflow.api.web;

import com.deptflow.application.documents.AddDocumentVersion;
import com.deptflow.application.documents.ApproveDocument;
import com.deptflow.application.documents.ArchiveDocument;
import com.deptflow.application.documents.CreateDocument;
import com.deptflow.application.documents.DocumentDtos;
import com.deptflow.application.documents.GetDocument;
import com.deptflow.application.documents.RejectDocument;
import com.deptflow.application.documents.SubmitForApproval;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    record RejectRequest(String comment) {
    }

    private final CreateDocument createDocument;
    private final AddDocumentVersion addDocumentVersion;
    private final SubmitForApproval submitForApproval;
    private final ApproveDocument approveDocument;
    private final RejectDocument rejectDocument;
    private final ArchiveDocument archiveDocument;
    private final GetDocument getDocument;

    public DocumentController(CreateDocument createDocument, AddDocumentVersion addDocumentVersion,
                              SubmitForApproval submitForApproval, ApproveDocument approveDocument,
                              RejectDocument rejectDocument, ArchiveDocument archiveDocument,
                              GetDocument getDocument) {
        this.createDocument = createDocument;
        this.addDocumentVersion = addDocumentVersion;
        this.submitForApproval = submitForApproval;
        this.approveDocument = approveDocument;
        this.rejectDocument = rejectDocument;
        this.archiveDocument = archiveDocument;
        this.getDocument = getDocument;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDtos.DocumentView create(
            @RequestParam("departmentId") UUID departmentId,
            @RequestParam("title") String title,
            @RequestParam(value = "documentTypeId", required = false) UUID documentTypeId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "changeNote", required = false) String changeNote,
            @RequestPart("file") MultipartFile file) throws IOException {
        DocumentDtos.CreateDocumentCommand command = new DocumentDtos.CreateDocumentCommand(
                departmentId, documentTypeId, title, description,
                file.getOriginalFilename(), file.getContentType(), file.getSize(),
                file.getInputStream(), changeNote);
        return createDocument.execute(command);
    }

    @PostMapping(value = "/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentDtos.DocumentVersionView addVersion(
            @PathVariable UUID id,
            @RequestParam(value = "changeNote", required = false) String changeNote,
            @RequestPart("file") MultipartFile file) throws IOException {
        DocumentDtos.AddDocumentVersionCommand command = new DocumentDtos.AddDocumentVersionCommand(
                id, file.getOriginalFilename(), file.getContentType(), file.getSize(),
                file.getInputStream(), changeNote);
        return addDocumentVersion.execute(command);
    }

    @PostMapping("/{id}/submit")
    public DocumentDtos.DocumentView submit(@PathVariable UUID id) {
        return submitForApproval.execute(new DocumentDtos.SubmitCommand(id));
    }

    @PostMapping("/{id}/approve")
    public DocumentDtos.DocumentView approve(@PathVariable UUID id) {
        return approveDocument.execute(new DocumentDtos.ApproveCommand(id));
    }

    @PostMapping("/{id}/reject")
    public DocumentDtos.DocumentView reject(@PathVariable UUID id, @RequestBody RejectRequest request) {
        return rejectDocument.execute(new DocumentDtos.RejectCommand(id, request.comment()));
    }

    @PostMapping("/{id}/archive")
    public DocumentDtos.DocumentView archive(@PathVariable UUID id) {
        return archiveDocument.execute(new DocumentDtos.ArchiveCommand(id));
    }

    @GetMapping("/{id}")
    public DocumentDtos.DocumentView get(@PathVariable UUID id) {
        return getDocument.execute(id);
    }
}
