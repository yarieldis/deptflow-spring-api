package com.deptflow.domain;

/**
 * Summary status of a {@link Document}, derived from the status of its latest
 * {@link DocumentVersion} and mutated only by the document aggregate.
 */
public enum DocumentStatus {
    DRAFT,
    PENDING_APPROVAL,
    PUBLISHED,
    ARCHIVED
}
