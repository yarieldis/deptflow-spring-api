package com.deptflow.application.exceptions;

/** Raised when an operation requires a tenant context but none is present (maps to HTTP 401/403). */
public class TenantRequiredException extends RuntimeException {

    public TenantRequiredException() {
        super("A tenant (institution) context is required");
    }
}
