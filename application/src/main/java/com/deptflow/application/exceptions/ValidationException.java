package com.deptflow.application.exceptions;

/** Raised for invalid input (maps to HTTP 422). */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
