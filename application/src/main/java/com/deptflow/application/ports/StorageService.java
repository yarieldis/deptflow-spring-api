package com.deptflow.application.ports;

import java.io.IOException;
import java.io.InputStream;

/**
 * Backend-agnostic file storage. The database stores only an opaque
 * {@code storageKey}; the concrete backend (filesystem, S3, Azure Blob) is
 * hidden behind this port.
 */
public interface StorageService {

    /** Stores the content and returns an opaque storage key. */
    String save(InputStream content);

    /** Opens a stored file for reading. */
    InputStream openRead(String storageKey) throws IOException;

    /** Deletes a stored file. */
    void delete(String storageKey);
}
