package com.deptflow.domain;

/**
 * Base exception for domain rule violations: invalid state transitions, broken
 * invariants, or required values that are missing. Raised by aggregates and
 * value objects; the API layer maps these to HTTP 4xx responses.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
