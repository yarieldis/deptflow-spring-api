package com.deptflow.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Small internal helpers used by domain factories and methods to enforce
 * required fields and normalized unique codes.
 */
final class DomainAssertions {

    private DomainAssertions() {
    }

    static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainException(field + " is required");
        }
        return value.trim();
    }

    /**
     * Normalizes a business code to lowercase so uniqueness behaves the same on
     * case-insensitive (SQL Server) and case-sensitive (PostgreSQL/SQLite) engines.
     */
    static String normalizeCode(String code) {
        return Codes.normalize(code);
    }

    static UUID requireId(UUID id, String field) {
        if (id == null) {
            throw new DomainException(field + " is required");
        }
        return id;
    }

    static LocalDate requireDate(LocalDate date, String field) {
        if (date == null) {
            throw new DomainException(field + " is required");
        }
        return date;
    }
}
