package com.deptflow.domain;

import java.util.Locale;

/**
 * Normalization helpers for unique business codes. Public so the application
 * layer can pre-check duplicates using the same normalization the domain
 * applies on write.
 */
public final class Codes {

    private Codes() {
    }

    public static String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainException("code is required");
        }
        return code.trim().toLowerCase(Locale.ROOT);
    }
}
