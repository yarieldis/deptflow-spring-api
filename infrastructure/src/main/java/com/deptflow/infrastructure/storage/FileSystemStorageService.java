package com.deptflow.infrastructure.storage;

import com.deptflow.application.exceptions.StorageException;
import com.deptflow.application.ports.StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * Filesystem-backed {@link StorageService}. Files live under a configured root
 * directory and are referenced by an opaque, generated {@code UUID} key.
 */
public class FileSystemStorageService implements StorageService {

    private final Path root;

    public FileSystemStorageService(String rootPath) {
        try {
            this.root = Path.of(rootPath).toAbsolutePath().normalize();
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not initialize storage root", e);
        }
    }

    @Override
    public String save(InputStream content) {
        String key = UUID.randomUUID().toString();
        Path target = resolve(key);
        try (InputStream in = content;
             OutputStream out = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
            in.transferTo(out);
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
        return key;
    }

    @Override
    public InputStream openRead(String storageKey) throws IOException {
        return Files.newInputStream(resolve(storageKey));
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException e) {
            throw new StorageException("Failed to delete file", e);
        }
    }

    private Path resolve(String storageKey) {
        if (storageKey == null || storageKey.contains("/") || storageKey.contains("\\") || storageKey.contains("..")) {
            throw new StorageException("Invalid storage key");
        }
        return root.resolve(storageKey);
    }
}
