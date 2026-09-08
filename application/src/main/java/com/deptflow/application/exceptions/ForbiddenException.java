package com.deptflow.application.exceptions;

/** Raised when the caller is not authorized to perform an operation (maps to HTTP 403). */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
