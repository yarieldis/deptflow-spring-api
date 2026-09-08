package com.deptflow.domain;

/**
 * Status of a single immutable {@link DocumentVersion}. Version-level approval
 * drives the document's summary status.
 */
public enum VersionStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}
