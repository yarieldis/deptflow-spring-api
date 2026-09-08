package com.deptflow.application.documents;

import com.deptflow.domain.Document;
import com.deptflow.domain.DocumentStatus;
import com.deptflow.domain.DocumentVersion;
import com.deptflow.domain.VersionStatus;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Request/response contracts for the document use cases. */
public final class DocumentDtos {

    private DocumentDtos() {
    }

    public record CreateDocumentCommand(
            UUID departmentId,
            UUID documentTypeId,
            String title,
            String description,
            String fileName,
            String contentType,
            long sizeBytes,
            InputStream content,
            String changeNote) {
    }

    public record AddDocumentVersionCommand(
            UUID documentId,
            String fileName,
            String contentType,
            long sizeBytes,
            InputStream content,
            String changeNote) {
    }

    public record SubmitCommand(UUID documentId) {
    }

    public record ApproveCommand(UUID documentId) {
    }

    public record RejectCommand(UUID documentId, String comment) {
    }

    public record ArchiveCommand(UUID documentId) {
    }

    public record DocumentVersionView(
            UUID id,
            int versionNumber,
            String storageKey,
            String fileName,
            String contentType,
            long sizeBytes,
            String changeNote,
            UUID createdByPersonId,
            Instant createdAt,
            VersionStatus status,
            Instant submittedAt,
            UUID submittedByPersonId,
            Instant reviewedAt,
            UUID reviewedByPersonId,
            String reviewComment) {

        public static DocumentVersionView from(DocumentVersion v) {
            return new DocumentVersionView(
                    v.getId(), v.getVersionNumber(), v.getStorageKey(), v.getFileName(),
                    v.getContentType(), v.getSizeBytes(), v.getChangeNote(), v.getCreatedByPersonId(),
                    v.getCreatedAt(), v.getStatus(), v.getSubmittedAt(), v.getSubmittedByPersonId(),
                    v.getReviewedAt(), v.getReviewedByPersonId(), v.getReviewComment());
        }
    }

    public record DocumentView(
            UUID id,
            UUID institutionId,
            UUID departmentId,
            UUID documentTypeId,
            String title,
            String description,
            DocumentStatus status,
            UUID createdByPersonId,
            Instant createdAt,
            Instant updatedAt,
            List<DocumentVersionView> versions) {

        public static DocumentView from(Document d) {
            return new DocumentView(
                    d.getId(), d.getInstitutionId(), d.getDepartmentId(), d.getDocumentTypeId(),
                    d.getTitle(), d.getDescription(), d.getStatus(), d.getCreatedByPersonId(),
                    d.getCreatedAt(), d.getUpdatedAt(),
                    d.getVersions().stream().map(DocumentVersionView::from).toList());
        }
    }
}
