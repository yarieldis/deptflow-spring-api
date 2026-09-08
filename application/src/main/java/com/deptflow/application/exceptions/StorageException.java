package com.deptflow.application.exceptions;

/** Raised when the storage backend cannot read, write, or delete a file. */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
